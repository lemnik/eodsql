package net.lemnik.eodsql.spi.util;

import java.lang.reflect.Type;
import java.lang.reflect.InvocationTargetException;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.LinkedHashSet;

import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TypeMapper;
import net.lemnik.eodsql.InvalidQueryException;
import net.lemnik.eodsql.InvalidDataTypeException;

import net.lemnik.eodsql.spi.Context;

import net.lemnik.eodsql.spi.util.DataObjectBinding.BindingType;

/**
 * <p>
 * A {@code ResultSetWrapper} allows is used to wrap a normal {@link java.sql.ResultSet} object
 * in the type requested by a {@link net.lemnik.eodsql.BaseQuery Query interface}. Implementations
 * of this class are <i>not</i> responsible for binding the actual data-objects, but instead
 * for creating their collection wrappers. Default implementations exist for the following types:
 * </p>
 * 
 * <ul>
 * <li>{@link net.lemnik.eodsql.DataSet}</li>
 * <li>{@link net.lemnik.eodsql.DataIterator}</li>
 * <li>{@link java.util.Set} - unmodifyable</li>
 * <li>{@link java.util.SortedSet} - unmodifyable</li>
 * <li>{@link java.util.List} - unmodifyable</li>
 * <li>{@link java.util.Collection} - unmodifyable (underpinned by a {@code List})</li>
 * <li>Arrays</li>
 * <li>Single Data Objects, or {@link net.lemnik.eodsql.QueryTool#getTypeMap() primitives}</li>
 * </ul>
 * 
 * <p>
 * A single {@code ResultSetWrapper} implementation may optionally return any number of
 * different types. For example the built in {@code CollectionResultSetWrapper} returns
 * {@code List}, {@code Set}, {@code SortedSet} and {@code Collection} implementations.
 * The {@code ResultSetWrapper} may also need to bind multiple data-objects per row (or none),
 * thus the {@link Factory} class exists to determine which {@code ResultSetWrapper} should
 * be used for a given set of parameters and a return-type.
 * </p>
 * 
 * @param <T> the wrapping type that this {@code ResultSetWrapper} will return
 * @author Jason Morris
 */
public abstract class ResultSetWrapper<T> {

    /**
     * The parameter that can be used to specify the {@link BindingType}. This parameter
     * should be one of the {@link BindingType} constants if it exists.
     * 
     * @see #getBindingType(java.util.Map)
     */
    public static final String PARAMETER_BINDING_TYPE =
            "net.lemnik.eodsql.spi.util.ResultSetWrapper#bindingType";

    /**
     * <p>
     * This parameter allows to set a custom class for data object binding for this query. The 
     * default value {@link NoDataObjectBinding} means: no custom binding is requested.
     * </p>
     */
    public static final String PARAMETER_CUSTOM_DATA_OBJECT_BINDING = "net.lemnik.eodsql.spi.util.ResultSetWrapper#binding";

    private static final Set<Factory> FACTORIES = new LinkedHashSet<Factory>(4);

    private static final Map<String, Object> NO_PARAMETERS =
            Collections.emptyMap();



    static {
        addFactory(new DataSetWrapper.Factory());
        addFactory(new DataIteratorWrapper.Factory());
        addFactory(new ArrayWrapper.Factory());
        addFactory(new CollectionWrapperFactory());

        try {
            @SuppressWarnings("unchecked")
            final Iterable<Factory> loader = (Iterable<Factory>)Class.forName(
                    "java.util.ServiceLoader").
                    getMethod("load", Class.class).
                    invoke(null, Factory.class);

            for(final Factory factory : loader) {
                addFactory(factory);
            }
        } catch(NoClassDefFoundError e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(IllegalArgumentException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(SecurityException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(IllegalAccessException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(InvocationTargetException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(NoSuchMethodException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(ClassNotFoundException e) {
            // ignore this... we are simply not running 1.6 or higher
        }
    }

    /**
     * The constructor for a {@code ResultSetWrapper}.
     */
    protected ResultSetWrapper() {
    }

    @SuppressWarnings("unchecked")
    private static ResultSetWrapper getResultSetWrapperForSimpleClass(
            final Class<?> type,
            final BindingType bindingType,
            final DataObjectBinding customBinding) {

        if (customBinding != null) {
            customBinding.setObjectType(type);
            return new SingleRowResultSetWrapper(customBinding);
        }
        
        final TypeMapper<?> mapper = QueryTool.getTypeMap().get(type);

        if(mapper != null) {
            return getTypeMapperResultSetWrapper(mapper, (Class)type);
        } else {
            return getDataObjectResultSetWrapper(type, bindingType);
        }
    }

    private static <T> ResultSetWrapper<T> getTypeMapperResultSetWrapper(
            final TypeMapper<T> mapper,
            final Class<T> type) {

        final DataObjectBinding<T> binding = new TypeMapperDataObjectBinding<T>(
                mapper,
                type);

        return new SingleRowResultSetWrapper<T>(binding);
    }

    private static <T> ResultSetWrapper getDataObjectResultSetWrapper(
            final Class<T> type,
            final DataObjectBinding.BindingType bindingType) {

        final DataObjectBinding<T> binding = DataObjectBinding.
                getDataObjectBinding(type, bindingType);

        return new SingleRowResultSetWrapper<T>(binding);
    }

    /**
     * <p>
     * This is a utility method for those {@code ResultSetWrapper} implementations that don't
     * need any of the additional information stored in a {@link Context} object, and want the
     * {@code Context} to be auto-closed when this method call exits.
     * </p><p>
     * By default this method throws an {@code UnsupportedOperationException}. If you do
     * override the {@link #wrap(net.lemnik.eodsql.spi.Context)} method, there is no reason
     * to override this method.
     * </p>
     * 
     * @param results the {@code ResultSet} to wrap
     * @return the {@code ResultSet} wrapped in a new wrapper object
     * @throws java.sql.SQLException if a database error occures
     * @see #wrap(net.lemnik.eodsql.spi.Context)
     */
    protected T wrap(final ResultSet results) throws SQLException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the {@code BindingType} value as specified in the given {@code parameters}. This
     * method will return {@code BindingType.NORMAL_BINDING} if no {@code BindingType} was
     * specified in the parameter {@code Map}.
     * 
     * @param parameters the parameters as specified in {@link #get(java.lang.reflect.Type, java.util.Map)}
     * @return the {@code BindingType} that should be used according to the parameter {@code Map}
     */
    protected static BindingType getBindingType(
            final Map<String, Object> parameters) {

        if(parameters.containsKey(PARAMETER_BINDING_TYPE)) {
            return (BindingType)parameters.get(PARAMETER_BINDING_TYPE);
        }

        return BindingType.NORMAL_BINDING;
    }

    /**
     * Returns the custom binding, or <code>null</code>, if no custom binding is specified in the 
     * parameter {@code Map}.
     * 
     * @param parameters the parameters as specified in {@link #get(java.lang.reflect.Type, java.util.Map)}
     * @return the {@code DataObjectBinding} that should be used according to the parameter {@code Map}
     */
    protected static DataObjectBinding<?> getCustomBinding(
            final Map<String, Object> parameters) {

        if(parameters.containsKey(DataSetWrapper.PARAMETER_CUSTOM_DATA_OBJECT_BINDING)) {
            return (DataObjectBinding<?>)parameters.get(DataSetWrapper.PARAMETER_CUSTOM_DATA_OBJECT_BINDING);
        }

        return null;
    }

    /**
     * Wrap the given {@code Context} in whatever type this {@code ResultSetWrapper} should
     * return. This method by default makes a call to {@link #wrap(java.sql.ResultSet)}, but can
     * be overriden if required (for example to switch of {@link Context#setAutoclose(boolean)
     * autoclose}).
     * 
     * @param context the full {@code Context} to wrap
     * @return the given {@code Context} wrapped in a new wrapper object
     * @throws java.sql.SQLException if a database error occures
     */
    public T wrap(final Context<?> context) throws SQLException {
        return wrap(context.getResource(ResultSet.class).get());
    }

    /**
     * <p>
     * Returns this {@code ResultSetWrapper} objects preferred type of {@code ResultSet}. This
     * method should return the same as should be returned by {@link java.sql.ResultSet#getType()}.
     * </p><p>
     * By default this method returns {@code ResultSet.TYPE_SCROLL_INSENSITIVE}, which is used
     * by most of the underlying implementations.
     * </p>
     * 
     * @return {@code ResultSet.TYPE_SCROLL_INSENSITIVE} by default
     */
    public int getPreferredResultSetType() {
        return ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    /**
     * <p>
     * Returns this {@code ResultSetWrapper} objects preferred concurrency for a {@code ResultSet}.
     * This method should return the value expected from
     * {@link java.sql.ResultSet#getConcurrency()}.
     * </p><p>
     * By default this method returns {@code ResultSet.CONCUR_READ_ONLY}, which is used by most
     * of the underlying implementations.
     * </p>
     * 
     * @return {@code ResultSet.CONCUR_READ_ONLY} by default
     */
    public int getPreferredResultSetConcurrency() {
        return ResultSet.CONCUR_READ_ONLY;
    }

    /**
     * If this {@code ResultSetWrapper} has a list of key column names that should be
     * requested from the database during an update, this method should return that list.
     * 
     * @return {@link DataObjectBinding#NO_KEY_COLUMNS} by default
     * @see DataObjectBinding#getKeyColumnNames()
     */
    public String[] getKeyColumnNames() {
        return DataObjectBinding.NO_KEY_COLUMNS;
    }

    /**
     * <p>
     * Hand register a {@link Factory} object. This method can be used under Java 1.5
     * where the {@code ServiceLoader} class is not avaiable. If a {@code Factory} of the
     * given type is already known, this method does nothing.
     * </p><p>
     * This method is <b>not</b> thread-safe, and should only ever be invoked by one
     * Thread at a time.
     * </p>
     * 
     * @param factory the {@code Factory} to add
     * @see Factory Factory - for more information
     */
    public static void addFactory(final Factory factory) {
        FACTORIES.add(factory);
    }

    /**
     * <p>
     * Remove a registered {@link Factory} object. This method will first check to see if the
     * specified {@code Factory} object is already registered, if it is, the {@code Factory}
     * will be removed from registration.
     * </p><p>
     * This method is <b>not</b> thread-safe, and should only ever be invoked by one
     * Thread at a time.
     * </p
     * 
     * @param factory the {@code Factory} to unregister
     * @see Factory Factory - for more information
     */
    public static void removeFactory(final Factory factory) {
        FACTORIES.add(factory);
    }

    /**
     * Validate the given return type, and throw an exception if it is found to be invalid.
     * If this method returns without an {@code Exception} being thrown, a call to
     * {@link #get(java.lang.reflect.Type, java.util.Map)} should return a valid
     * {@code ResultSetWrapper}.
     * 
     * @param genericReturnType the return type to validate
     * @throws net.lemnik.eodsql.InvalidDataTypeException if the {@code genericReturnType} cannot
     *      be wrapped by a {@code ResultSetWrapper}
     * @throws net.lemnik.eodsql.InvalidQueryException if the {@code genericReturnType} cannot
     *      be wrapped by a {@code ResultSetWrapper}
     */
    public static void validate(final Type genericReturnType)
            throws InvalidDataTypeException,
            InvalidQueryException {

        if(genericReturnType instanceof Class &&
                !((Class<?>)genericReturnType).isArray()) {

            final Class<?> type = (Class<?>)genericReturnType;

            if(!QueryTool.getTypeMap().containsKey(type)) {
                DataObjectBinding.validate(type);
            }
        } else {
            for(Factory f : FACTORIES) {
                if(f.isTypeConstructable(genericReturnType, NO_PARAMETERS)) {
                    return;
                }
            }

            throw new InvalidDataTypeException(
                    "Cannot find a ResultSetWrapper to handle type",
                    genericReturnType);
        }
    }

    /**
     * <p>
     * Returns a {@code ResultSetWrapper} for the given {@code genericReturnType} and
     * parameter {@code Map}. This method will first check to see if the {@code genericReturnType}
     * is a {@link net.lemnik.eodsql.QueryTool#getTypeMap() primitive type} or a
     * {@link net.lemnik.eodsql.spi.util.DataObjectBinding data-object}, if it is a special
     * {@code ResultSetWrapper} implementation will be used.
     * </p><p>
     * If the returned objects are not "simple" (as defined above), the method will look
     * <b>backwards</b> through the list of {@link Factory} objects. As soon as a
     * {@link Factory#isTypeConstructable(java.lang.reflect.Type, java.util.Map)} call returns
     * {@literal true}, this method will use that {@code Factory}'s
     * {@link Factory#create(java.lang.reflect.Type, java.util.Map) create} method and
     * return the resulting {@code ResultSetWrapper}.
     * </p><p>
     * If no {@code Factory} would be able to create a wrapper for the given parameters, this
     * method will return {@literal null}.
     * </p>
     * 
     * @param genericReturnType the generic type that needs to be returned from the method
     *      implementation
     * @param parameters a {@code Map} of parameters that will be passed to the
     *      {@link Factory}
     * @return a {@code ResultSetWrapper} that will wrap a {@code ResultSet} according to the
     *      given parameter {@code Map} and return the requested type
     * @throws InvalidDataTypeException if the {@link Factory} cannot construct a
     *      {@link net.lemnik.eodsql.spi.util.DataObjectBinding data-object} of the requested type
     * @throws InvalidQueryException if the {@link Factory} cannot create a wrapper for the
     *      given parameter {@code Map}
     */
    public static ResultSetWrapper get(
            final Type genericReturnType,
            final Map<String, Object> parameters)
            throws InvalidDataTypeException, InvalidQueryException {

        if(genericReturnType instanceof Class &&
                !((Class<?>)genericReturnType).isArray()) {

            final Class<?> type = (Class<?>)genericReturnType;

            return getResultSetWrapperForSimpleClass(
                    type,
                    getBindingType(parameters),
                    getCustomBinding(parameters));
        } else {
            for(final Factory f : FACTORIES) {
                if(f.isTypeConstructable(genericReturnType, parameters)) {
                    return f.create(genericReturnType, parameters);
                }
            }
        }

        throw new InvalidDataTypeException(
                "Cannot find a ResultSetWrapper to handle type",
                genericReturnType);
    }

    /**
     * <p>
     * Each {@code ResultSetWrapper} implementation will need at least one {@code Factory}
     * implementation. The {@code Factory} class is used to decide:
     * </p>
     * 
     * <ol>
     * <li>Whether a given type can be constructed by the {@code ResultSetWrapper}</li>
     * <li>If the parameter {@code Map} given is valid for the requested {@code Type}</li>
     * </ol>
     * 
     * <p>
     * Factories can be registered using the {@link java.util.ServiceLoader} class, or by
     * hand using the {@link 
     * ResultSetWrapper#addFactory(net.lemnik.eodsql.spi.util.ResultSetWrapper.Factory)
     * ResultSetWrapper.addFactory}
     * method. When deciding which {@code Factory} to use, the {@code ResultSetWrapper} will
     * look <b>backwards</b> through the list of registered {@code Factory} objects. This means
     * that the {@code Factory} added last has the highest priority.
     * </p>
     */
    public static interface Factory {

        /**
         * <p>
         * Tests to see if this {@code Factory} object can build a {@code ResultSetWrapper} that
         * will produce implementations of the requested type. This method may also validate
         * any generic parameters, and the parameters in the parameter {@code Map} against any
         * rules that it needs followed.
         * </p><p>
         * If the {@code Factory} cannot produce an implementation because the {@code genericType}
         * is wrong, or another {@code Factory} will be able to, this method should simply
         * return {@literal false}.
         * </p><p>
         * If however: the {@code Factory} would normally be able to construct the implementation,
         * but there are contracts or rules violated by the parameters (ie: a {@code DataSet}
         * being requested as {@literal updatable} <i>and</i> {@literal disconnected}), this
         * method should throw an Exception to indicate the violation.
         * </p><p>
         * Implementations of {@code Factory} must be thread-safe.
         * </p>
         * 
         * @param genericType the description of the type that should be returned from by the
         *      {@link ResultSetWrapper#wrap(net.lemnik.eodsql.spi.Context)} method
         * @param parameters the parameter {@code Map} holding additional information for the
         *      {@code ResultSetWrapper} object
         * @return {@literal true} if this {@code Factory} can create a {@code ResultSetWrapper}
         *      for the given type and parameter {@code Map}
         * @throws net.lemnik.eodsql.InvalidQueryException if the type or parameters violate a
         *      contract of this {@code Factory} object
         */
        public boolean isTypeConstructable(
                Type genericType,
                Map<String, Object> parameters)
                throws InvalidQueryException;

        /**
         * Request that a {@code ResultSetWrapper} be created for the given type and
         * parameters. This method will only ever be called after a call to
         * {@link #isTypeConstructable(java.lang.reflect.Type, java.util.Map)} returns
         * {@literal true} when given the same parameters.
         * 
         * @param genericType the generic type to wrap
         * @param parameters the custom parameters for the {@code ResultSetWrapper}
         * @return a valid {@code ResultSetWrapper} based on the type and parameter {@code Map},
         *      never {@literal null}
         */
        public ResultSetWrapper create(
                Type genericType,
                Map<String, Object> parameters);

    }
}
