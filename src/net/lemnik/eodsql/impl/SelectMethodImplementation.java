package net.lemnik.eodsql.impl;

import java.lang.reflect.Type;
import java.lang.reflect.Method;

import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.IdentityHashMap;

import java.text.ParseException;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.InvalidQueryException;
import net.lemnik.eodsql.Select;

import net.lemnik.eodsql.spi.Context;
import net.lemnik.eodsql.spi.MethodImplementation;
import net.lemnik.eodsql.spi.MethodImplementationFactory;
import net.lemnik.eodsql.spi.ResultSetResource;

import net.lemnik.eodsql.spi.util.NoDataObjectBinding;
import net.lemnik.eodsql.spi.util.Query;
import net.lemnik.eodsql.spi.util.ResultSetWrapper;
import net.lemnik.eodsql.spi.util.DataIteratorWrapper;

import net.lemnik.eodsql.spi.util.DataSetWrapper;

/**
 * Created on 2008/05/19
 * @author Jason Morris
 */
class SelectMethodImplementation extends AbstractMethodImplementation<Select> {

    private final Integer fetchSize;

    SelectMethodImplementation(final Method method) throws ParseException {
        final Select select = method.getAnnotation(Select.class);

        String queryString = select.value();

        if(queryString.length() == 0) {
            queryString = select.sql();
        }

        query = Query.getQuery(queryString, method.getParameterTypes());

        setParameterMappers(select.parameterBindings());
        
        final Map<String, Object> parameters =
                extractReturnTypeMapperParameters(select);

        final Type returnType = method.getGenericReturnType();
        wrapper = ResultSetWrapper.get(returnType, parameters);

        if(returnType instanceof Class && !((Class)returnType).isArray()) {
            fetchSize = 1;
        } else {
            fetchSize = select.fetchSize() != 0
                    ? select.fetchSize()
                    : null;
        }
    }

    private static Map<String, Object> extractReturnTypeMapperParameters(
            final Select select) {

        final Map<String, Object> parameters =
                new IdentityHashMap<String, Object>();

        if(select.disconnected()) {
            parameters.put(DataSetWrapper.PARAMETER_DISCONNECTED, Boolean.TRUE);
        } else {
            parameters.put(DataSetWrapper.PARAMETER_DISCONNECTED, Boolean.FALSE);
        }

        if(select.readOnly()) {
            parameters.put(DataSetWrapper.PARAMETER_UPDATABLE, Boolean.FALSE);
        } else {
            parameters.put(DataSetWrapper.PARAMETER_UPDATABLE, Boolean.TRUE);
        }

        if(select.rubberstamp()) {
            parameters.put(
                    DataIteratorWrapper.PARAMETER_RUBBERSTAMP,
                    Boolean.TRUE);
        } else {
            parameters.put(
                    DataIteratorWrapper.PARAMETER_RUBBERSTAMP,
                    Boolean.FALSE);
        }

        if (select.resultSetBinding() != NoDataObjectBinding.class) {
            try {
                parameters.put(DataSetWrapper.PARAMETER_CUSTOM_DATA_OBJECT_BINDING, 
                        select.resultSetBinding().newInstance());
            } catch (Exception ex)
            {
                throw new InvalidQueryException("DataObjectBinding classes must have a " +
                        "default (null) constructor: " + select.resultSetBinding().getCanonicalName(), ex);
            }
        }
        
        parameters.put(DataSetWrapper.PARAMETER_CACHE_CLASS, select.cache());

        return parameters;
    }

    public void invoke(final Context<Select> context) throws Throwable {
        final PreparedStatement statement = createPreparedStatement(context);

        if(fetchSize != null) {
            statement.setFetchSize(fetchSize);
        }

        fillPreparedStatementParameters(context, statement);

        final ResultSet results = statement.executeQuery();
        context.setResource(new ResultSetResource(results));

        context.setReturnValue(wrapper.wrap(context));
    }

    static final class Factory implements MethodImplementationFactory<Select> {

        public void validate(final Method method) throws InvalidQueryException {
            final Select select = method.getAnnotation(Select.class);
            String sql = select.value();

            if(sql.length() == 0) {
                sql = select.sql();
            }

            if(sql.length() == 0) {
                throw new InvalidQueryException(
                        "No EoD SQL Query in Select annotation", method);
            }

            Query.validate(sql, method);

            if(select.disconnected() && !select.readOnly()) {
                throw new InvalidQueryException(
                        "A disconnected Select may not be writable.",
                        method);
            }

            if(select.rubberstamp() && method.getReturnType() != DataIterator.class) {
                throw new InvalidQueryException(
                        "A rubberstamping Select must return a " +
                        "DataIterator", method);
            }

            ResultSetWrapper.validate(method.getGenericReturnType());
        }

        public MethodImplementation<Select> createImplementation(final Method method) {
            try {
                return new SelectMethodImplementation(method);
            } catch(ParseException pe) {
                // shouldn't happen, the Query.validate method should have kicked up a fuss
                throw new EoDException(pe);
            }
        }

    }
}
