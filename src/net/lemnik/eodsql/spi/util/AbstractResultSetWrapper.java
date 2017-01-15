package net.lemnik.eodsql.spi.util;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import java.util.Set;

import net.lemnik.eodsql.InvalidQueryException;

/**
 * This class simply adds the methods to deal with
 * a DataObjectBinding to ResultSetWrapper.
 * 
 * Created on 2008/07/21
 * @author Jason Morris
 */
abstract class AbstractResultSetWrapper<T, V> extends ResultSetWrapper<T> {

    protected DataObjectBinding<V> binding;

    protected AbstractResultSetWrapper(final DataObjectBinding<V> binding) {
        this.binding = binding;
    }

    protected DataObjectBinding<V> getDataObjectBinding() {
        return binding;
    }

    @Override
    public String[] getKeyColumnNames() {
        return binding.getKeyColumnNames();
    }

    static Class<?> getDataObjectClass(
            final Type genericType,
            final Set<Class> outerTypes) {

        if(genericType instanceof ParameterizedType) {
            final ParameterizedType paramType = (ParameterizedType)genericType;

            if(outerTypes.contains(paramType.getRawType())) {
                final Type[] args = paramType.getActualTypeArguments();

                if(args.length != 1 || !(args[0] instanceof Class)) {
                    throw new InvalidQueryException(
                            "Generic must have a single solid type-argument");
                }

                final Class<?> clazz = (Class<?>)args[0];
                DataObjectBinding.validate(clazz);

                return clazz;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "[" +
                binding +
                "]";
    }

}
