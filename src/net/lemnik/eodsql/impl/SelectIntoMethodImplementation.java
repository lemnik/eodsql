package net.lemnik.eodsql.impl;

import java.lang.reflect.Method;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;

import java.text.ParseException;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.NoResultException;

import net.lemnik.eodsql.spi.Context;
import net.lemnik.eodsql.spi.ResultSetResource;
import net.lemnik.eodsql.spi.StatementResource;

import net.lemnik.eodsql.spi.util.Query;
import net.lemnik.eodsql.spi.util.DataObjectBinding;

/**
 * This method implementation will run a query against the database, and
 * put the resulting data into an exsting object.
 *
 * @author Jason Morris
 */
class SelectIntoMethodImplementation extends AbstractMethodImplementation<Select> {

    /**
     * The index of the parameter we are going to populate and return. This
     * is zero indexed like an array.
     */
    private final int parameterIndex;

    private final DataObjectBinding<Object> binding;

    @SuppressWarnings("unchecked")
    SelectIntoMethodImplementation(final Method method) throws ParseException {
        final Select select = method.getAnnotation(Select.class);

        String queryString = select.value();

        if(queryString.length() == 0) {
            queryString = select.sql();
        }

        final Class<?>[] paramTypes = method.getParameterTypes();

        query = Query.getQuery(queryString, paramTypes);
        parameterIndex = select.into() - 1;
        
        final Class<?> parameterType = paramTypes[parameterIndex];
        binding = (DataObjectBinding<Object>)
                DataObjectBinding.getDataObjectBinding(
                parameterType,
                DataObjectBinding.BindingType.NORMAL_BINDING);
    }

    public void invoke(final Context<Select> context) throws Throwable {
        final Object into = context.getParameters()[parameterIndex];

        if(into == null) {
            throw new NullPointerException("Parameter may not be null.");
        }

        final Connection connection = context.getResource(Connection.class).get();
        final PreparedStatement statement = connection.prepareStatement(
                query.toString(),
                ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);

        statement.setMaxRows(1);

        context.setResource(new StatementResource(statement));

        fillPreparedStatementParameters(context, statement);

        final ResultSet results = statement.executeQuery();
        context.setResource(new ResultSetResource(results));

        if(results.next()) {
            binding.unmarshall(results, into);
            context.setReturnValue(into);
        } else {
            throw new NoResultException();
        }
    }

}
