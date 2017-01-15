package net.lemnik.eodsql.spi.util;

import net.lemnik.eodsql.spi.Context;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Set;
import java.util.Map;
import java.util.Collections;

import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.DataSetCache;
import net.lemnik.eodsql.ArrayDataSetCache;

import net.lemnik.eodsql.InvalidDataTypeException;
import net.lemnik.eodsql.InvalidQueryException;

/**
 * <p>
 * The Wrapper implementation for wrapping a {@code ResultSet} in a
 * {@link net.lemnik.eodsql.DataSet}. This class is exposed to allow access to it's
 * {@code PARAMETER_*} constants.
 * </p><p>
 * Note that a {@code DataSet} cannot be updatable and disconnected at the same time.
 * </p>
 * 
 * @author Jason Morris
 */
public class DataSetWrapper extends ResultSetWrapper<DataSet> {

    /**
     * <p>
     * The parameter constant that describes whether a {@code DataSet} should be disconnected
     * before it is returned. The value of this parameter should be a {@link java.lang.Boolean}
     * object.
     * </p><p>
     * Setting this parameter to true will result in an optimized implementation of
     * {@code DataSet} being returned. The {@code DataSet} will first jump to the end
     * of the {@code ResultSet} in order to find the length, it will then return to the
     * begining and read in each object. The entire {@code Context} will then be disconnected.
     * </p>
     */
    public static final String PARAMETER_DISCONNECTED =
            "net.lemnik.eodsql.spi.util.DataSetWrapper#disconnected";

    /**
     * <p>
     * This parameter determines whether a the returned {@code DataSet} objects will be updateable.
     * The value of this parameter should be a {@link java.lang.Boolean} object.
     * If a {@code DataSet} is updateable, it's {@link java.util.List#add(java.lang.Object)},
     * {@link java.util.List#addAll(java.util.Collection)}, {@link java.util.List#remove(int)}
     * and {@link java.util.List#set(int, java.lang.Object)} methods will be implemented.
     * </p><p>
     * An updatable {@code DataSet} relies on an {@link java.sql.ResultSet#CONCUR_UPDATABLE
     * updatable ResultSet}, and thus on the same rules. In general you will need to
     * {@literal SELECT} from only one table, and include the entire {@literal PRIMARY KEY}.
     * </p>
     */
    public static final String PARAMETER_UPDATABLE =
            "net.lemnik.eodsql.spi.util.DataSetWrapper#updatable";

    /**
     * <p>
     * This parameter determines the {@link net.lemnik.eodsql.DataSetCache} implementation that
     * the returned {@code DataSet} objects should use. By default this is a
     * {@link net.lemnik.eodsql.ArrayDataSetCache}, but setting the value of the parameter
     * to a {@link java.lang.Class} object that is a valid {@code DataSetCache} implementation
     * with a public default constrctor will result in the returned {@code DataSet} objects
     * using that implementation instead.
     * </p>
     */
    public static final String PARAMETER_CACHE_CLASS =
            "net.lemnik.eodsql.spi.util.DataSetWrapper#cacheClass";

    private boolean disconnected = false;

    private boolean updatable = false;

    private Class<? extends DataSetCache> cacheClass = ArrayDataSetCache.class;

    private DataObjectBinding binding;

    @SuppressWarnings("unchecked")
    private DataSetWrapper(
            final Class dataObjectType,
            final Map<String, Object> parameters) {

        if (parameters.containsKey(PARAMETER_CUSTOM_DATA_OBJECT_BINDING)) {
            final DataObjectBinding<Object> 
                customBinding = (DataObjectBinding<Object>) parameters.get(PARAMETER_CUSTOM_DATA_OBJECT_BINDING);
            customBinding.setObjectType(dataObjectType);
            binding = customBinding;
        } else {
        binding = DataObjectBinding.getDataObjectBinding(dataObjectType,
                AbstractResultSetWrapper.getBindingType(parameters));
        }

        disconnected = isDisconnected(parameters);
        updatable = isUpdatable(parameters);

        if(parameters.containsKey(PARAMETER_CACHE_CLASS)) {
            cacheClass = (Class<? extends DataSetCache>)parameters.get(
                    PARAMETER_CACHE_CLASS);

            if(!DataSetCache.class.isAssignableFrom(cacheClass)) {
                throw new IllegalArgumentException(
                        "The Cache Class parameter must be a Class of " +
                        "type DataSetCache.");
            }
        }
    }

    private DataSetCache createCache() throws SQLException {
        try {
            return cacheClass.newInstance();
        } catch(final InstantiationException ie) {
            throw (SQLException)(new SQLException()).initCause(ie);
        } catch(final IllegalAccessException iae) {
            throw (SQLException)(new SQLException()).initCause(iae);
        }
    }

    private static boolean isDisconnected(final Map<String, Object> parameters) {
        if(parameters.containsKey(PARAMETER_DISCONNECTED)) {
            return parameters.get(PARAMETER_DISCONNECTED).equals(Boolean.TRUE);
        }

        return false;
    }

    private static boolean isUpdatable(final Map<String, Object> parameters) {
        if(parameters.containsKey(PARAMETER_UPDATABLE)) {
            return parameters.get(PARAMETER_UPDATABLE).equals(Boolean.TRUE);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataSet wrap(final Context context) throws SQLException {
        if(disconnected) {
            @SuppressWarnings("unchecked")
            final DataSet<?> dataSet = new DisconnectedDataSet(
                    binding,
                    context);

            return dataSet;
        } else if(updatable) {
            context.setAutoclose(false);
            @SuppressWarnings("unchecked")
            final DataSet<?> dataSet = new UpdatableDataSet(
                    binding,
                    context,
                    createCache());

            return dataSet;
        } else {
            context.setAutoclose(false);
            @SuppressWarnings("unchecked")
            final DataSet<?> dataSet = new ConnectedDataSet(
                    binding,
                    context,
                    createCache());

            return dataSet;
        }
    }

    /**
     * If the {@code DataSet} objects returned by this {@code DataSetWrapper} are to be
     * updatable, this method will return {@link java.sql.ResultSet#CONCUR_UPDATABLE}.
     * Otherwise this method returned the default {@link java.sql.ResultSet#CONCUR_READ_ONLY}.
     * 
     * @return the desired concurrency of a {@code ResultSet} to be wrapped
     * @see #PARAMETER_UPDATABLE
     */
    @Override
    public int getPreferredResultSetConcurrency() {
        return updatable
                ? ResultSet.CONCUR_UPDATABLE
                : ResultSet.CONCUR_READ_ONLY;
    }

    /**
     * When we make an update to the {@code ResultSet} we want to see the
     * changes in that {@code ResultSet}. This implementation will
     * return {@link ResultSet#TYPE_SCROLL_SENSITIVE} for updatable
     * {@code DataSet}s, and {@link ResultSet#TYPE_SCROLL_INSENSITIVE}
     * for read-only {@code DataSet}s.
     *
     * @return type preferred type of {@code ResultSet} that we would
     *      like to wrap
     */
    @Override
    public int getPreferredResultSetType() {
        return updatable
                ? ResultSet.TYPE_SCROLL_SENSITIVE
                : ResultSet.TYPE_SCROLL_INSENSITIVE;
    }

    static final class Factory implements ResultSetWrapper.Factory {

        private static final Set<Class> DATA_SET_TYPE =
                Collections.singleton((Class)DataSet.class);

        @SuppressWarnings("unchecked")
        public boolean isTypeConstructable(
                final Type genericType,
                final Map<String, Object> parameters)
                throws InvalidQueryException {

            final Class<?> clazz = AbstractResultSetWrapper.getDataObjectClass(
                    genericType,
                    DATA_SET_TYPE);

            if(clazz == null) {
                return false;
            }

            if(parameters.containsKey(PARAMETER_CACHE_CLASS)) {
                final Object cacheObject = parameters.get(PARAMETER_CACHE_CLASS);

                if(!(cacheObject instanceof Class) ||
                        !DataSetCache.class.isAssignableFrom((Class)cacheObject)) {

                    throw new InvalidQueryException("Parameter '" +
                            PARAMETER_CACHE_CLASS +
                            "' must be a Class<? extends DataSetCache>");
                }

                validateDataSetCache((Class<? extends DataSetCache>)cacheObject);
            }


            if(isUpdatable(parameters)) {
                if(isDisconnected(parameters)) {
                    throw new InvalidQueryException(
                            "A disconnected DataSet may not be updatable.");
                }
                @SuppressWarnings("unchecked")
                final DataObjectBinding<?> binding =
                        DataObjectBinding.getDataObjectBinding(
                        clazz,
                        AbstractResultSetWrapper.getBindingType(parameters));

                if(!binding.isUpdateCapable()) {
                    throw new InvalidDataTypeException(
                            "DataSet is updatable, but " +
                            "the data-type is not able to update.",
                            clazz);
                }
            }

            return true;
        }

        private void validateDataSetCache(
                final Class<? extends DataSetCache> cacheClass) {

            try {
                cacheClass.getConstructor();
            } catch(final NoSuchMethodException ex) {
                throw new InvalidQueryException(
                        "Cannot find a null constrcutor in type: " +
                        cacheClass.getName());
            } catch(final SecurityException ex) {
                throw new InvalidQueryException(
                        "Cannot access the null constrcutor in type: " +
                        cacheClass.getName() +
                        ", try making it public.");
            }
        }

        public ResultSetWrapper create(
                final Type genericType,
                final Map<String, Object> parameters) {

            final ParameterizedType parameterType =
                    (ParameterizedType)genericType;
            final Class dataType =
                    (Class)parameterType.getActualTypeArguments()[0];

            return new DataSetWrapper(dataType, parameters);
        }

    }
}
