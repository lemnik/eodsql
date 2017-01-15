package net.lemnik.eodsql.impl;

import java.lang.reflect.Method;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.CallableStatement;

import java.text.ParseException;

import java.util.Map;
import java.util.IdentityHashMap;

import net.lemnik.eodsql.Call;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.InvalidQueryException;

import net.lemnik.eodsql.spi.Context;
import net.lemnik.eodsql.spi.ResultSetResource;
import net.lemnik.eodsql.spi.StatementResource;
import net.lemnik.eodsql.spi.MethodImplementation;
import net.lemnik.eodsql.spi.MethodImplementationFactory;

import net.lemnik.eodsql.spi.util.NoDataObjectBinding;
import net.lemnik.eodsql.spi.util.Query;
import net.lemnik.eodsql.spi.util.DataSetWrapper;
import net.lemnik.eodsql.spi.util.ResultSetWrapper;
import net.lemnik.eodsql.spi.util.DataIteratorWrapper;

/**
 * Created on 2008/07/23
 * @author Jason Morris
 */
class CallMethodImplementation extends AbstractMethodImplementation<Call> {
    private final boolean returnVoid;

    CallMethodImplementation(final Method method) throws ParseException {
        final Call call = method.getAnnotation(Call.class);

        String queryString = call.value();

        if(queryString.length() == 0) {
            queryString = call.call();
        }

        query = Query.getQuery(queryString, method.getParameterTypes());
        
        setParameterMappers(call.parameterBindings());
        
        returnVoid = method.getReturnType() == Void.TYPE;

        if(!returnVoid) {
            final Map<String, Object> parameters = extractReturnTypeParameters(call);
            wrapper = ResultSetWrapper.get(method.getGenericReturnType(), parameters);
        }
    }

    private static Map<String, Object> extractReturnTypeParameters(final Call call) {
        final Map<String, Object> parameters = new IdentityHashMap<String, Object>(4);

        if(call.disconnected()) {
            parameters.put(DataSetWrapper.PARAMETER_DISCONNECTED, Boolean.TRUE);
        } else {
            parameters.put(DataSetWrapper.PARAMETER_DISCONNECTED, Boolean.FALSE);
        }

        if(call.readOnly()) {
            parameters.put(DataSetWrapper.PARAMETER_UPDATABLE, Boolean.FALSE);
        } else {
            parameters.put(DataSetWrapper.PARAMETER_UPDATABLE, Boolean.TRUE);
        }

        if(call.rubberstamp()) {
            parameters.put(DataIteratorWrapper.PARAMETER_RUBBERSTAMP, Boolean.TRUE);
        } else {
            parameters.put(DataIteratorWrapper.PARAMETER_RUBBERSTAMP, Boolean.FALSE);
        }

        if (call.resultSetBinding() != NoDataObjectBinding.class) {
            try {
                parameters.put(DataSetWrapper.PARAMETER_CUSTOM_DATA_OBJECT_BINDING, 
                        call.resultSetBinding().newInstance());
            } catch (Exception ex)
            {
                throw new InvalidQueryException("DataObjectBinding classes must have a " +
                        "default (null) constructor: " + call.resultSetBinding().getCanonicalName(), ex);
            }
        }
        
        parameters.put(DataSetWrapper.PARAMETER_CACHE_CLASS, call.cache());

        return parameters;
    }

    public void invoke(final Context<Call> context) throws Throwable {
        final Connection connection = context.getResource(Connection.class).get();

        // if we return void, we don't have a wrapper and so can't get result set
        // type or concurrency from the wrapper object.
        final CallableStatement statement = returnVoid ? 
                connection.prepareCall(query.toString()) :
                    connection.prepareCall(query.toString(),
                    wrapper.getPreferredResultSetType(),
                    wrapper.getPreferredResultSetConcurrency());

        fillPreparedStatementParameters(context, statement);

        context.setResource(new StatementResource(statement));

        if(statement.execute()) {
            // if we do return void, we still want to close the ResultSet
            final ResultSet results = statement.getResultSet();
            context.setResource(new ResultSetResource(results));

            if(!returnVoid) {
                context.setReturnValue(wrapper.wrap(context));
            }
        }
    }

    static final class Factory implements MethodImplementationFactory<Call> {
        public void validate(final Method method) throws InvalidQueryException {
            final Call call = method.getAnnotation(Call.class);

            String sql = call.value();

            if(sql.length() == 0) {
                sql = call.call();
            }

            if(sql.length() == 0) {
                throw new InvalidQueryException("No EoD SQL Query in Call annotation", method);
            }

            Query.validate(sql, method);

            if(call.rubberstamp() && method.getReturnType() != DataIterator.class) {
                throw new InvalidQueryException("A rubberstamping Select must return a " +
                        "DataIterator", method);
            }

            if(method.getReturnType() != Void.TYPE) {
                ResultSetWrapper.validate(method.getGenericReturnType());
            }
        }

        public MethodImplementation<Call> createImplementation(final Method method) {
            try {
                return new CallMethodImplementation(method);
            } catch(ParseException pe) {
                // shouldn't happen, the Query.validate method should have kicked up a fuss
                throw new EoDException(pe);
            }
        }

    }
}
