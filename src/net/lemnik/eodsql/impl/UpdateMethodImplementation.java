package net.lemnik.eodsql.impl;

import java.lang.reflect.Method;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.text.ParseException;

import java.util.Map;
import java.util.Collections;

import net.lemnik.eodsql.Update;
import net.lemnik.eodsql.GeneratedKeys;
import net.lemnik.eodsql.InvalidQueryException;

import net.lemnik.eodsql.spi.Context;
import net.lemnik.eodsql.spi.ResultSetResource;
import net.lemnik.eodsql.spi.StatementResource;
import net.lemnik.eodsql.spi.MethodImplementation;
import net.lemnik.eodsql.spi.MethodImplementationFactory;

import net.lemnik.eodsql.spi.util.Query;
import net.lemnik.eodsql.spi.util.ResultSetWrapper;
import net.lemnik.eodsql.spi.util.DataObjectBinding.BindingType;

import static net.lemnik.eodsql.spi.util.DataSetWrapper.*;

/**
 * Created on 2008/06/28
 * @author Jason Morris
 */
class UpdateMethodImplementation extends AbstractMethodImplementation<Update> {

    private final GeneratedKeys keys;

    UpdateMethodImplementation(final Method method) throws ParseException {
        final Update update = method.getAnnotation(Update.class);

        String queryString = update.value();

        if(queryString.length() == 0) {
            queryString = update.sql();
        }

        keys = update.keys();

        query = Query.getQuery(queryString, getParameterTypes(method));

        setParameterMappers(update.parameterBindings());
        
        final Map<String, Object> parameters =
                extractReturnTypeMapperParameters(update);

        if(keys != GeneratedKeys.NO_KEYS_RETURNED) {
            wrapper = ResultSetWrapper.get(
                    method.getGenericReturnType(),
                    parameters);
        }
    }

    protected Class<?>[] getParameterTypes(final Method method) {
        return method.getParameterTypes();
    }

    private Map<String, Object> extractReturnTypeMapperParameters(
            final Update update) {

        BindingType bindingType = BindingType.KEYS_BINDING;

        if(update.keys() == GeneratedKeys.RETURNED_KEYS_FIRST_COLUMN) {
            bindingType = BindingType.FIRST_COLUMN_BINDING;
        }

        return Collections.singletonMap(
                PARAMETER_BINDING_TYPE,
                (Object)bindingType);
    }

    public void invoke(final Context<Update> context) throws Throwable {
        final Connection connection = context.
                getResource(Connection.class).get();
        
        PreparedStatement statement = null;

        switch(keys) {
            case RETURNED_KEYS_COLUMNS_SPECIFIED:
                statement = connection.prepareStatement(
                        query.toString(),
                        wrapper.getKeyColumnNames());
                break;
            case RETURNED_KEYS_DRIVER_DEFINED:
            // fallthrough
            case RETURNED_KEYS_FIRST_COLUMN:
                statement = connection.prepareStatement(
                        query.toString(),
                        PreparedStatement.RETURN_GENERATED_KEYS);
                break;
            case NO_KEYS_RETURNED:
            default:
                statement = connection.prepareStatement(query.toString());
        }

        context.setResource(new StatementResource(statement));

        update(statement, context);

        if(keys != GeneratedKeys.NO_KEYS_RETURNED) {
            final ResultSet results = statement.getGeneratedKeys();
            context.setResource(new ResultSetResource(results));
            context.setReturnValue(wrapper.wrap(context));
        } else {
            context.setReturnValue(Integer.valueOf(statement.getUpdateCount()));
        }
    }

    protected void update(
            final PreparedStatement statement,
            final Context<Update> context)
            throws SQLException {

        fillPreparedStatementParameters(context, statement);
        statement.executeUpdate();
    }

    static final class Factory implements MethodImplementationFactory<Update> {

        public void validate(final Method method) throws InvalidQueryException {
            final Update update = method.getAnnotation(Update.class);

            String sql = update.value();

            if(sql.length() == 0) {
                sql = update.sql();
            }

            if(sql.length() == 0) {
                throw new InvalidQueryException("No EoD SQL Query in " +
                        "Select annotation", method);
            }

            if(update.keys() == GeneratedKeys.NO_KEYS_RETURNED &&
                    (method.getReturnType() != Void.TYPE ||
                    method.getReturnType() == Integer.TYPE ||
                    method.getReturnType() == Integer.class)) {

                throw new InvalidQueryException("An Update method returning " +
                        "GeneratedKeys.NO_KEYS_RETURNED must have a return " +
                        "type of void or int.", method);
            }

            if(update.batchUpdate()) {
                BatchUpdateMethodImplementation.createParameterViewFactories(
                        method);
            }

            Query.validate(sql, method);

            // TODO: Validate that we can do some sort of key binding in the return type
        }

        public MethodImplementation<Update> createImplementation(
                final Method method) {

            try {
                final Update update = method.getAnnotation(Update.class);
                
                if(update.batchUpdate()) {
                    return new BatchUpdateMethodImplementation(method);
                } else {
                    return new UpdateMethodImplementation(method);
                }
            } catch(final ParseException ex) {
                // won't happen... will have thrown an
                // error in the "validate" method.
                return null;
            }
        }

    }
}
