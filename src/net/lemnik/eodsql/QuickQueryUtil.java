package net.lemnik.eodsql;

import java.lang.annotation.Annotation;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.text.ParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import net.lemnik.eodsql.spi.Context;
import net.lemnik.eodsql.spi.Resource;
import net.lemnik.eodsql.spi.ResultSetResource;
import net.lemnik.eodsql.spi.StatementResource;

import net.lemnik.eodsql.spi.util.NoDataObjectBinding;
import net.lemnik.eodsql.spi.util.DataObjectBinding;
import net.lemnik.eodsql.spi.util.Query;
import net.lemnik.eodsql.spi.util.DataSetWrapper;
import net.lemnik.eodsql.spi.util.ResultSetWrapper;
import net.lemnik.eodsql.spi.util.DataObjectBinding.BindingType;

/**
 * Created on 2008/07/31
 * @author Jason Morris
 */
class QuickQueryUtil {

    private static final Map<String, Object> DEFAULT_DATASET_PARAMETERS;
    
    static {
        final Map<String, Object> map = new HashMap<String, Object>(2);
        map.put(DataSetWrapper.PARAMETER_BINDING_TYPE, BindingType.NORMAL_BINDING);
        map.put(DataSetWrapper.PARAMETER_UPDATABLE, true);
        DEFAULT_DATASET_PARAMETERS = Collections.unmodifiableMap(map);
    }

    private static final Select DEFAULT_SELECT_PARAMETERS = new Select() {

        public String value() {
            return "";
        }

        public String sql() {
            return "";
        }

        public boolean disconnected() {
            return false;
        }

        public boolean rubberstamp() {
            return false;
        }

        public boolean readOnly() {
            return false;
        }

        public Class<? extends DataSetCache> cache() {
            return ArrayDataSetCache.class;
        }

        public Class<? extends Annotation> annotationType() {
            return Select.class;
        }

        public int fetchSize() {
            return 0;
        }

        public int into() {
            return 0;
        }

        public Class<? extends DataObjectBinding> resultSetBinding()
        {
            return NoDataObjectBinding.class;
        }

        public Class<? extends TypeMapper>[] parameterBindings()
        {
            return new Class[0];
        }

    };

    private static final Update DEFAULT_UPDATE_PARAMETERS = new Update() {

	public boolean batchUpdate() {
	    return false;
	}

	public GeneratedKeys keys() {
	    return GeneratedKeys.NO_KEYS_RETURNED;
	}

	@SuppressWarnings("unchecked")
	public Class<? extends TypeMapper>[] parameterBindings() {
	    return new Class[0];
	}

	public String sql() {
	    return "";
	}

	public String value() {
	    return "";
	}

	public Class<? extends Annotation> annotationType() {
	    return Update.class;
	}
    };
	
    private QuickQueryUtil() {
    }

    private static Class<?>[] getParameterTypes(final Object[] parameters) {
        final int parameterCount = parameters.length;
        final Class<?>[] parameterTypes = new Class<?>[parameterCount];

        for(int i = 0; i < parameterCount; i++) {
            parameterTypes[i] = parameters[i].getClass();
        }

        return parameterTypes;
    }

    private static Type wrapClassInDataSet(final Class<?> simpleType) {
        return new ParameterizedType() {

            private Class[] args = new Class[]{simpleType};

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getRawType() {
                return DataSet.class;
            }

            public Type getOwnerType() {
                return null;
            }

        };
    }

    private static void fillStatementParameters(
            final Query eodquery,
            final PreparedStatement statement,
            final Context<?> context)
            throws SQLException {

        final Map<Class, TypeMapper> mappers = QueryTool.getTypeMap();
        final int parameterCount = eodquery.getParameterCount();

        for(int i = 0; i < parameterCount; i++) {
            final Class<?> type = eodquery.getParameterType(i);
            @SuppressWarnings("unchecked")
            final TypeMapper<Object> mapper = mappers.get(type);

            if(mapper != null) {
                mapper.set(statement, i + 1, eodquery.getParameter(context, i));
            } else {
                throw new InvalidQueryException(
                        "No TypeMapper found for " + type);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> DataSet<T> selectDataSet(
            final Connection connection,
            final boolean closeConnection,
            final Class<T> dataObject,
            final String query,
            final Object... parameters)
            throws SQLException, ParseException {

        DataSet<T> output = null;

        final Context<Select> context = new Context<Select>(
                DEFAULT_SELECT_PARAMETERS,
                parameters);

        try {
            final ResultSetWrapper<DataSet> wrapper = ResultSetWrapper.get(
                    wrapClassInDataSet(dataObject), DEFAULT_DATASET_PARAMETERS);
            final Class<?>[] parameterTypes = getParameterTypes(parameters);

            context.setResource(new ConnectionResource(connection,
                    closeConnection));
            final Query eodquery = Query.getQuery(query, parameterTypes);
            final PreparedStatement statement = connection.prepareStatement(
                    eodquery.toString(),
                    wrapper.getPreferredResultSetType(),
                    wrapper.getPreferredResultSetConcurrency());

            fillStatementParameters(eodquery, statement, context);
            context.setResource(new StatementResource(statement));

            final ResultSet results = statement.executeQuery();
            context.setResource(new ResultSetResource(results));

            output = wrapper.wrap(context);
        } finally {
            if(output == null) {
                context.close();
            }
        }

        return output;
    }

    public static int update(
            final Connection connection,
            final boolean closeConnection,
            final String query,
            final Object... parameters)
            throws SQLException, ParseException {

        final Context<Update> context = new Context<Update>(
                DEFAULT_UPDATE_PARAMETERS,
                parameters);

        try {
            final Class<?>[] parameterTypes = getParameterTypes(parameters);

            context.setResource(new ConnectionResource(connection, closeConnection));
            final Query eodquery = Query.getQuery(query, parameterTypes);
            final PreparedStatement statement = connection.prepareStatement(
                    eodquery.toString());

            fillStatementParameters(eodquery, statement, context);
            context.setResource(new StatementResource(statement));
            return statement.executeUpdate();
        } finally {
            context.close();
        }
    }

    private static class ConnectionResource implements Resource<Connection> {

        private Connection connection;

        private boolean close = true;

        public ConnectionResource(Connection connection, boolean close) {
            this.connection = connection;
            this.close = close;
        }

        public Connection get() {
            return connection;
        }

        public boolean isClosed() {
            return connection == null;
        }

        public void close() throws SQLException {
            if(close) {
                connection.close();
            }

            connection = null;
        }

        public Class<Connection> getResourceType() {
            return Connection.class;
        }

    }
}
