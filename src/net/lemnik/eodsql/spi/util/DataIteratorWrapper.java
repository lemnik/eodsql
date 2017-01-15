package net.lemnik.eodsql.spi.util;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Set;
import java.util.Map;
import java.util.Collections;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.InvalidQueryException;
import net.lemnik.eodsql.InvalidDataTypeException;

import net.lemnik.eodsql.spi.Context;

/**
 * The Wrapper implementation for {@link net.lemnik.eodsql.DataIterator} objects. This class
 * is exposed mainly for the {@link #PARAMETER_RUBBERSTAMP} constant.
 * 
 * @author Jason Morris
 */
public class DataIteratorWrapper extends ResultSetWrapper<DataIterator<?>> {
    /**
     * The rubberstamp parameter constant. The value of this parameter should be a
     * {@link java.lang.Boolean} object. In order for rubberstamping to work, the underlying
     * {@link DataObjectBinding} must be rubbertsmpaing capable otherwise an
     * {@link InvalidDataTypeException} will be thrown.
     */
    public static final String PARAMETER_RUBBERSTAMP = "net.elmnik.eodsql.spi.util.DataIteratorWrapper#rubberstamp";

    private boolean rubberstamping = false;

    private DataObjectBinding<?> binding;

    private DataIteratorWrapper(
            final DataObjectBinding<?> binding,
            final Map<String, Object> parameters) {
        
        this.binding = binding;
        this.rubberstamping = isRubberstamping(parameters);
    }

    private static boolean isRubberstamping(
            final Map<String, Object> parameters) {
        
        if(parameters.containsKey(PARAMETER_RUBBERSTAMP)) {
            if(parameters.get(PARAMETER_RUBBERSTAMP).equals(Boolean.TRUE)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataIterator<?> wrap(final Context<?> context) throws SQLException {
        context.setAutoclose(false);

        if(rubberstamping) {
            @SuppressWarnings("unchecked")
            final RubberstampingDataIterator iterator =
                    new RubberstampingDataIterator(context, binding);

            return iterator;
        } else {
            @SuppressWarnings("unchecked")
            final DefaultDataIterator iterator =
                    new DefaultDataIterator(context, binding);
            
            return iterator;
        }
    }

    @Override
    public int getPreferredResultSetType() {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    static class Factory implements ResultSetWrapper.Factory {
        private static final Set<Class> DATA_ITERATOR_TYPE =
                Collections.singleton((Class)DataIterator.class);

        public boolean isTypeConstructable(
                final Type genericType,
                final Map<String, Object> parameters)
                throws InvalidQueryException {

            final Class<?> clazz = AbstractResultSetWrapper.
                    getDataObjectClass(genericType, DATA_ITERATOR_TYPE);

            if(clazz == null) {
                return false;
            }

            if(isRubberstamping(parameters)) {
                @SuppressWarnings("unchecked")
                final DataObjectBinding<?> binding = DataObjectBinding.
                        getDataObjectBinding(
                        clazz,
                        AbstractResultSetWrapper.getBindingType(parameters));

                if(!binding.isRubberstampCapable()) {
                    throw new InvalidDataTypeException(
                            "DataIterator is rubberstamping, " +
                            "but the data-type is not" +
                            "able to rubberstamp.", clazz);
                }
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        public ResultSetWrapper create(
                final Type genericType,
                final Map<String, Object> parameters) {

            final ParameterizedType parameterType = (ParameterizedType)genericType;
            final Class<?> clazz = (Class<?>)parameterType.getActualTypeArguments()[0];

            final DataObjectBinding<?> binding;
            if (parameters.containsKey(PARAMETER_CUSTOM_DATA_OBJECT_BINDING)) {
                final DataObjectBinding<Object> 
                    customBinding = (DataObjectBinding<Object>) parameters.get(PARAMETER_CUSTOM_DATA_OBJECT_BINDING);
                customBinding.setObjectType((Class<Object>) clazz);
                binding = customBinding;
            } else {
                binding = DataObjectBinding.getDataObjectBinding(
                    clazz,
                    AbstractResultSetWrapper.getBindingType(parameters));
            }

            return new DataIteratorWrapper(binding, parameters);
        }

    }
}
