package net.lemnik.eodsql.spi.util;

import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.InvocationTargetException;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.NoSuchElementException;

import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.InvalidQueryException;
import net.lemnik.eodsql.InvalidDataTypeException;

import net.lemnik.eodsql.spi.util.DataObjectBinding.BindingType;

import static net.lemnik.eodsql.spi.util.ResultSetWrapper.PARAMETER_BINDING_TYPE;
import static net.lemnik.eodsql.spi.util.ResultSetWrapper.PARAMETER_CUSTOM_DATA_OBJECT_BINDING;

/**
 * <p>
 * A little backwards from other {@link ResultSetWrapper}s this class is
 * declared as the factory, while various implementations reside within
 * it. This {@code ResultSetWrapper.Factory} handles generic declarations
 * of {@link List}, {@link Collection}, {@link SortedSet}, and {@link Set}.
 * It also handles when a concrete {@code Collection} implementation is
 * declared as the wrapper type.
 * </p><p>
 * When a concrete {@code Collection} implementation is declared as the
 * wrapper this class allows for two construction mechanisms. If the
 * implementation has a {@code Implementation(Collection)} style
 * constructor, it will be used. If the implementation only has a
 * default constructor, the {@link Collection#addAll(Collection)} method
 * will be called to populate the {@code Collection} object.
 * </p>
 * 
 * @author Jason Morris
 */
class CollectionWrapperFactory implements ResultSetWrapper.Factory {

    /*
     * Design Note: Unlike previous implementations of the Collection
     * wrapping code, this class uses the ResultSetCollection class
     * for all of it's translation needs. The ResultSetCollection is
     * a bit like a simplified version of ConnectedDataSet.
     *
     * Take a look at the AbstractCollectionWrapper to see how it's
     * expected to work.
     */
    /**
     * Inspects the given {@code Class} and determines whether it is
     * a constructable {@code Collection} by a {@code CollectionWrapperFactory}.
     *
     * @param clazz the {@code Collection} type to test
     * @return {@literal true} if there is a {@code CollectionWrapper} declared
     *      in {@code CollectionWrapperFactory} that will be able to create
     *      instances of the given {@code Class}
     */
    private boolean isCollectionConstructable(final Class<?> clazz) {
        if(clazz.equals(List.class) ||
                clazz.equals(Collection.class) ||
                clazz.equals(Set.class) ||
                clazz.equals(SortedSet.class)) {

            return true;
        } else if(!clazz.isInterface()) {
            for(final Constructor<?> c : clazz.getConstructors()) {
                final Class<?>[] params = c.getParameterTypes();

                if(params.length == 0 ||
                        (params.length == 1 &&
                        params[0].equals(Collection.class))) {

                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Performs all of the checks to make sure that the given {@code Type}
     * can be constructed by a {@code CollectionWrapper} implementation.
     * This method will first check that the {@code Type} represents a
     * parameterized {@code Collection}, and then validate that declaration.
     *
     * @param genericType the type that needs to be instantiated to wrap
     *      {@code ResultSet} objects
     * @param parameters the additional parameters passed through by the
     *      Factory classes
     * @return {@literal true} if we can create an instance of the given
     *      {@code Collection Type}, {@literal false} if it's not a
     *      parameterized {@code Collection}
     * @throws InvalidQueryException if the {@code Type} is a parameterized
     *      {@code Collection} that cannot be constructed or has an invalid
     *      type parameter
     */
    public boolean isTypeConstructable(
            final Type genericType,
            final Map<String, Object> parameters)
            throws InvalidQueryException {

        if(genericType instanceof ParameterizedType) {
            final ParameterizedType ptype = (ParameterizedType)genericType;
            final Type rawType = ptype.getRawType();

            if(rawType instanceof Class) {
                final Class<?> clazz = (Class<?>)rawType;

                if(Collection.class.isAssignableFrom(clazz)) {
                    final Type[] args = ptype.getActualTypeArguments();

                    if(args.length != 1) {
                        throw new InvalidQueryException(
                                "A Collection needs to have exactly " +
                                "one type parameter.");
                    }

                    if(args[0] instanceof Class) {
                        final Class<?> dataObject = (Class<?>)args[0];

                        if(SortedSet.class.isAssignableFrom(clazz) &&
                                !Comparable.class.isAssignableFrom(dataObject)) {

                            throw new InvalidDataTypeException(
                                    "DataType " + dataObject.getName() +
                                    " cannot be returned in a " +
                                    clazz.getName() + " because" +
                                    " it does not implement Comparable.",
                                    dataObject);
                        }

                        return isCollectionConstructable(clazz);
                    } else {
                        throw new InvalidQueryException(
                                "A Collection's generic argument must be " +
                                "a concrete class type.");
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public ResultSetWrapper create(
            final Type genericType,
            final Map<String, Object> parameters) {

        final ParameterizedType ptype = (ParameterizedType)genericType;
        final Class<?> clazz = (Class<?>)ptype.getRawType();

        final BindingType bindingType =
                parameters.containsKey(PARAMETER_BINDING_TYPE)
                ? (BindingType)parameters.get(PARAMETER_BINDING_TYPE)
                : BindingType.NORMAL_BINDING;

        final Class<?> dataObject = (Class<?>)ptype.getActualTypeArguments()[0];

        final DataObjectBinding binding;
        if (parameters.containsKey(PARAMETER_CUSTOM_DATA_OBJECT_BINDING)) {
            final DataObjectBinding<Object> 
                customBinding = (DataObjectBinding<Object>) parameters.get(PARAMETER_CUSTOM_DATA_OBJECT_BINDING);
            customBinding.setObjectType((Class<Object>) dataObject);
            binding = customBinding;
        } else {
            binding = DataObjectBinding.getDataObjectBinding(dataObject, bindingType);
        }

        if(clazz.equals(Collection.class) || clazz.equals(List.class)) {
            return new ListWrapper(binding);
        } else if(clazz.equals(Set.class)) {
            return new SetWrapper(binding);
        } else if(clazz.equals(SortedSet.class)) {
            return new SortedSetWrapper(binding);
        } else {
            try {
                clazz.getConstructor(Collection.class);

                return new CollectionConstructorWrapper(
                        binding,
                        (Class<? extends Collection>)clazz);
            } catch(final NoSuchMethodException nsme) {
                // couldn't find a nice Collection constructor, use the default
                return new CollectionDefaultWrapper(
                        binding,
                        (Class<? extends Collection>)clazz);
            } catch(final SecurityException se) {
                throw new EoDException(se);
            }
        }
    }

    private static abstract class AbstractCollectionWrapper<T extends Collection>
            extends AbstractResultSetWrapper<T, Object> {

        public AbstractCollectionWrapper(
                final DataObjectBinding<Object> binding) {

            super(binding);
        }

        protected abstract T newCollectionInstance(Collection<?> objects);

        @Override
        protected T wrap(final ResultSet results) throws SQLException {
            return newCollectionInstance(new ResultSetCollection<Object>(
                    results,
                    binding));
        }

    }

    private static class ListWrapper extends AbstractCollectionWrapper<List> {

        public ListWrapper(final DataObjectBinding<Object> binding) {
            super(binding);
        }

        @Override
        protected List newCollectionInstance(final Collection<?> objects) {
            return new ArrayList<Object>(objects);
        }

    }

    private static class SetWrapper extends AbstractCollectionWrapper<Set> {

        public SetWrapper(final DataObjectBinding<Object> binding) {
            super(binding);
        }

        @Override
        protected Set newCollectionInstance(final Collection<?> objects) {
            return new HashSet<Object>(objects);
        }

    }

    private static class SortedSetWrapper extends AbstractCollectionWrapper<SortedSet> {

        public SortedSetWrapper(
                final DataObjectBinding<Object> binding) {

            super(binding);
        }

        @Override
        protected SortedSet newCollectionInstance(final Collection<?> objects) {
            return new TreeSet<Object>(objects);
        }

    }

    private static abstract class UnknownCollectionWrapper extends AbstractCollectionWrapper<Collection> {

        protected final Class<? extends Collection> collectionType;

        public UnknownCollectionWrapper(
                final DataObjectBinding<Object> binding,
                final Class<? extends Collection> collectionType) {

            super(binding);
            this.collectionType = collectionType;
        }

    }

    private static class CollectionConstructorWrapper extends UnknownCollectionWrapper {

        private final Constructor<? extends Collection> constructor;

        public CollectionConstructorWrapper(
                final DataObjectBinding<Object> binding,
                final Class<? extends Collection> collectionType) {

            super(binding, collectionType);

            try {
                this.constructor = collectionType.getConstructor(
                        Collection.class);
            } catch(final NoSuchMethodException nsme) {
                // we will have tested for the constructor before attempting to
                // create an UnknownCollectionConstructorWrapper object
                throw new EoDException(nsme);
            } catch(final SecurityException se) {
                throw new EoDException(se);
            }
        }

        @Override
        protected Collection newCollectionInstance(
                final Collection<?> objects) {

            try {
                return constructor.newInstance(objects);
            } catch(final InstantiationException ie) {
                throw new EoDException(ie);
            } catch(final IllegalAccessException iae) {
                throw new EoDException(iae);
            } catch(final IllegalArgumentException iae) {
                throw new EoDException(iae);
            } catch(final InvocationTargetException ite) {
                throw new EoDException(ite.getTargetException());
            }
        }

    }

    private static class CollectionDefaultWrapper extends UnknownCollectionWrapper {

        public CollectionDefaultWrapper(
                final DataObjectBinding<Object> binding,
                final Class<? extends Collection> collectionType) {

            super(binding, collectionType);
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Collection newCollectionInstance(
                final Collection<?> objects) {

            try {
                final Collection collection = collectionType.newInstance();
                collection.addAll(objects);
                return collection;
            } catch(final InstantiationException ie) {
                throw new EoDException(ie);
            } catch(final IllegalAccessException iae) {
                throw new EoDException(iae);
            } catch(final IllegalArgumentException iae) {
                throw new EoDException(iae);
            }
        }

    }

    /**
     * This is a very simple {@code Collection} implementation that wraps
     * a live {@code ResultSet}. This class is passed into the
     * {@code Collection} objects created by the {@code ResultSetWrapper}
     * implementations, allowing the underlying implementation to fetch
     * the {@code ResultSet} in the manner that most fits that specific
     * implementation.
     *
     * @param <E> the element type to construct from the {@code ResultSet}
     */
    private static class ResultSetCollection<E> extends AbstractCollection<E> {

        /*
         * Currently this implementation will need a scrollable ResultSet,
         * which we may not have access to.
         *
         * Things that should be fixed:
         * - if the ResultSet is not able to scroll about like a mad thing,
         * we should pre-load the data into an array (see the ArrayWrapper
         * class for inspiration).
         * - we should support toArray implementations with a reverse
         * running ResultSet (again, look at ArrayWrapper).
         *
         */
        private final ResultSet results;

        private final DataObjectBinding<E> binding;

        /**
         * We cache the size of the {@code ResultSet} here.
         */
        private Integer size = null;

        /**
         * Create a new {@code ResultSetCollection} wrapping a specified
         * {@code ResultSet} using the given {@code DataObjectBinding}
         * to create the element objects for each row.
         *
         * @param results the {@code ResultSet} to wrap
         * @param binding used to create the element objects
         */
        ResultSetCollection(
                final ResultSet results,
                final DataObjectBinding<E> binding) {

            this.results = results;
            this.binding = binding;
        }

        @Override
        public Iterator<E> iterator() {
            try {
                if(!results.isBeforeFirst()) {
                    results.beforeFirst();
                }
            } catch(final SQLException sqle) {
                throw new EoDException(sqle);
            }

            return new Iterator<E>() {

                E next = null;

                boolean finished = false;

                public boolean hasNext() {
                    if(!finished && next == null) {
                        try {
                            if(results.next()) {
                                next = binding.unmarshall(results);
                            } else {
                                finished = true;
                            }
                        } catch(final SQLException sqle) {
                            throw new EoDException(sqle);
                        }
                    }

                    return !finished;
                }

                public E next() {
                    if(hasNext()) {
                        final E object = next;
                        next = null;
                        return object;
                    }

                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

            };
        }

        @Override
        public int size() {
            if(size == null) {
                try {
                    size = results.last()
                            ? results.getRow()
                            : Integer.valueOf(0);
                } catch(final SQLException sqle) {
                    throw new EoDException(sqle);
                }
            }

            return size != null
                    ? size
                    : 0;
        }

    }
}
