package net.lemnik.eodsql.impl;

import java.lang.reflect.Type;
import java.lang.reflect.Method;

import java.text.ParseException;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.InvalidQueryException;

import net.lemnik.eodsql.spi.MethodImplementation;
import net.lemnik.eodsql.spi.MethodImplementationFactory;

import net.lemnik.eodsql.spi.util.Query;
import net.lemnik.eodsql.spi.util.ResultSetWrapper;
import net.lemnik.eodsql.spi.util.DataObjectBinding;

/**
 * Created on Sep 16, 2009
 * @author Jason Morris
 */
class SelectMethodImplementationFactory implements MethodImplementationFactory<Select> {

    public void validate(final Method method) throws InvalidQueryException {
        final Select select = method.getAnnotation(Select.class);
        String sql = select.value();

        if(sql.length() == 0) {
            sql = select.sql();
        }

        if(sql.length() == 0) {
            throw new InvalidQueryException(
                    "No EoD SQL Query in " +
                    "Select annotation",
                    method);
        }

        Query.validate(sql, method);

        final Type returnType = method.getGenericReturnType();
        final Type[] params = method.getGenericParameterTypes();

        if(select.into() > 0 && select.into() <= params.length) {
            final Type param = params[select.into() - 1];
            if(!returnType.equals(void.class) && !returnType.equals(param)) {
                throw new InvalidQueryException(
                        "@Select(into) for parameter type " +
                        params[select.into() - 1] + " cannot return " +
                        returnType,
                        method);
            }

            if(param instanceof Class<?>) {
                DataObjectBinding.validate((Class<?>)param);
            } else {
                throw new InvalidQueryException(
                        "@Select(into) requires a valid, " +
                        "concrete data object type to select into: " + param,
                        method);
            }
        } else if(select.into() == 0) {
            if(returnType.equals(DataSet.class) &&
                    select.disconnected() &&
                    !select.readOnly()) {

                throw new InvalidQueryException(
                        "A disconnected Select may not be writable.",
                        method);
            }

            if(select.rubberstamp() && !returnType.equals(DataIterator.class)) {
                throw new InvalidQueryException(
                        "A rubberstamping Select must return a " +
                        "DataIterator", method);
            }

            ResultSetWrapper.validate(returnType);
        } else {
            throw new InvalidQueryException(
                    "Invalid parameter index to select into: " +
                    select.into(),
                    method);
        }
    }

    public MethodImplementation<Select> createImplementation(
            final Method method) {

        try {
        	Select select = method.getAnnotation(Select.class);
        	if (select.into() <= 0) {
        		return new SelectMethodImplementation(method);
        	} else {
        		return new SelectIntoMethodImplementation(method);
        	}
        } catch(ParseException pe) {
            // shouldn't happen, the Query.validate method
            // should have kicked up a fuss
            throw new EoDException(pe);
        }
    }

}
