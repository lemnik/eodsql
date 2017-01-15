package net.lemnik.eodsql.impl;

import java.lang.reflect.Type;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;

import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.text.ParseException;

import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractCollection;
import java.util.NoSuchElementException;

import net.lemnik.eodsql.Update;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.InvalidQueryException;

import net.lemnik.eodsql.spi.Context;

/**
 * Implementation of {@link Update} for batch updates.
 *
 * @author Bernd Rinn
 */
class BatchUpdateMethodImplementation extends UpdateMethodImplementation {

    private final ParameterViewFactory[] viewFactories;

    private final int firstIndexOfFiniteCollection;

    BatchUpdateMethodImplementation(final Method method) throws ParseException {
        super(method);
        viewFactories = createParameterViewFactories(method);
        firstIndexOfFiniteCollection = findFirstIndexOfFiniteCollection(viewFactories);
        if (firstIndexOfFiniteCollection == -1) {
            throw new RuntimeException("Method '" + method.getDeclaringClass().getSimpleName()
                    + "." + method.getName()
                    + "' supposed to do batch update, but has no batch parameter.");
        }
    }

    private Iterator<?>[] createParameterViews(final Object[] parameters) {
        final int parameterCount = parameters.length;
        final Iterator<?>[] views = new Iterator<?>[parameterCount];

        int size = -1;

        for(int i = 0; i < parameterCount; i++) {
            final Collection<?> v = viewFactories[i].createView(parameters[i]);
            final int parameterSize = v.size();

            if(parameterSize != -1) {
                if(size == -1) {
                    size = parameterSize;
                } else if(size != parameterSize) {
                    throw new EoDException("Batch parameter is mismatched " +
                            "in size: " + i);
                }
            }

            views[i] = v.iterator();
        }

        return views;
    }

    private Iterator<Context<Update>> iterate(final Context<Update> ctx) {
        final Iterator<?>[] views = createParameterViews(ctx.getParameters());

        if(views.length == 0) {
            return Collections.<Context<Update>>emptyList().iterator();
        } else {
            return new Iterator<Context<Update>>() {

                private final int paramCount = views.length;

                private final Object[] parameters = new Object[paramCount];

                /**
                 * The {@code Context} class will hold a reference to our
                 * parameters array, thus updating the array will update
                 * the parameters given in each {@code next()} call.
                 */
                private final Context<Update> context = new Context<Update>(
                        ctx, parameters);

                public boolean hasNext() {
                    return views[firstIndexOfFiniteCollection].hasNext();
                }

                public Context<Update> next() {
                    for(int i = 0; i < paramCount; i++) {
                        parameters[i] = views[i].next();
                    }

                    return context;
                }

                public void remove() {
                    throw new UnsupportedOperationException("Not supported.");
                }

            };
        }
    }

    private static ParameterViewFactory createParameterViewFactory(
            final Class<?> type) {

        if(type.isArray()) {
            return new ArrayViewFactory();
        } else if(Collection.class.isAssignableFrom(type)) {
            return new CollectionViewFactory();
        } else {
            return new SimpleParameterIteratorFactory();
        }
    }

    static ParameterViewFactory[] createParameterViewFactories(
            final Method method) {

        final Class<?>[] parameterTypes = method.getParameterTypes();
        final int parameterCount = parameterTypes.length;
        final ParameterViewFactory[] factories =
                new ParameterViewFactory[parameterCount];

        for(int i = 0; i < parameterCount; i++) {
            factories[i] = createParameterViewFactory(parameterTypes[i]);
        }

        return factories;
    }

    private static int findFirstIndexOfFiniteCollection(ParameterViewFactory[] viewFactories) {
        for (int i = 0; i < viewFactories.length; ++i)
        {
            if (viewFactories[i].isFiniteCollection())
            {
                return i;
            }
        }
        return -1;
    }

    @Override
    protected Class<?>[] getParameterTypes(final Method method) {
        final Type[] genericTypes = method.getGenericParameterTypes();
        final int parameterCount = genericTypes.length;
        final Class<?>[] classes = new Class<?>[parameterCount];

        for(int i = 0; i < parameterCount; i++) {
            final Type type = genericTypes[i];
            Class<?> clazz = null;

            if(type instanceof Class<?>) {
                clazz = (Class<?>)type;

                if(clazz.isArray()) {
                    clazz = clazz.getComponentType();
                }
            } else if(type instanceof ParameterizedType) {
                final ParameterizedType ptype = (ParameterizedType)type;

                if(ptype.getRawType() instanceof Class) {
                    final Class<?> raw = (Class<?>)ptype.getRawType();

                    if(Iterable.class.isAssignableFrom(raw)) {
                        final Type[] args = ptype.getActualTypeArguments();

                        if(args.length != 1) {
                            throw new InvalidQueryException(
                                    "A generic Iterable may only have " +
                                    "one type argument: " + ptype,
                                    method);
                        } else if(args[0] instanceof Class) {
                            clazz = (Class<?>)args[0];
                        } else {
                            throw new InvalidQueryException(
                                    "A generic Iterable must have a " +
                                    "concrete parameter type: " + ptype,
                                    method);
                        }
                    } else {
                        clazz = raw;
                        
                        if(clazz.isArray()) {
                            clazz = clazz.getComponentType();
                        }
                    }
                } else {
                    throw new InvalidQueryException(
                            "A generic type must has a concrete " +
                            "class type: " + type,
                            method);
                }
            } else if(type instanceof GenericArrayType) {
                final GenericArrayType gtype = (GenericArrayType)type;
                final Type component = gtype.getGenericComponentType();

                if(component instanceof Class) {
                    clazz = (Class)component;
                } else {
                    throw new InvalidQueryException(
                            "Arrays must have a concrete " +
                            "component type: " + gtype,
                            method);
                }
            } else {
                throw new InvalidQueryException(
                        "Unsupported type: " + type,
                        method);
            }

            classes[i] = clazz;
        }

        return classes;
    }

    @Override
    protected void update(
            final PreparedStatement statement,
            final Context<Update> context)
            throws SQLException {

        final Iterator<Context<Update>> iterator = iterate(context);

        while(iterator.hasNext()) {
            final Context<Update> ctx = iterator.next();

            fillPreparedStatementParameters(ctx, statement);
            statement.addBatch();
        }

        statement.executeBatch();
    }

    /**
     * Factory for creating an iterator for a batch update parameter. This iterator
     * will deliver all the values that will be used for that parameter in the batch
     * update.
     *
     * @author Bernd Rinn
     */
    interface ParameterViewFactory {

        static final int UNKNOWN_SIZE = -1;

        /**
         * Creates an iterator for the given batch update parameter.
         *
         * @param parameter The parameter of the batch update method to get the iterator for.
         *
         * @return a new iterator for the parameter.
         */
        Collection<?> createView(Object parameter);

        /**
         * Returns <code>true</code> if this view is a collection with a finite number of members.
         */
        boolean isFiniteCollection();

    }

    static abstract class IteratingCollection<T>
            extends AbstractCollection<T>
            implements Iterator<T> {

        @Override
        public Iterator<T> iterator() {
            return this;
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }

    private static class ArrayViewFactory implements ParameterViewFactory {

        public Collection<?> createView(final Object parameter) {
            return new IteratingCollection<Object>() {

                private final int size = Array.getLength(parameter);

                private int i = 0;

                public boolean hasNext() {
                    return i < size;
                }

                public Object next() {
                    if(!hasNext()) {
                        throw new NoSuchElementException();
                    }

                    return Array.get(parameter, i++);
                }

                @Override
                public int size() {
                    return size;
                }

            };
        }

        public boolean isFiniteCollection()
        {
            return true;
        }

    }

    private static class CollectionViewFactory implements ParameterViewFactory {

        public Collection<?> createView(final Object parameter) {
            return ((Collection<?>)parameter);
        }

        public boolean isFiniteCollection()
        {
            return true;
        }

    }

    private static class SimpleParameterIteratorFactory
            implements ParameterViewFactory {

        public Collection<?> createView(final Object parameter) {
            return new IteratingCollection<Object>() {

                public boolean hasNext() {
                    return true;
                }

                public Object next() {
                    return parameter;
                }

                @Override
                public int size() {
                    return UNKNOWN_SIZE;
                }

            };
        }

        public boolean isFiniteCollection()
        {
            return false;
        }

    }
}
