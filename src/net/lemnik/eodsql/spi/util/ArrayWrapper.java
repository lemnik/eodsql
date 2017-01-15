package net.lemnik.eodsql.spi.util;

import java.lang.reflect.Type;
import java.lang.reflect.Array;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Map;

import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.InvalidDataTypeException;

import net.lemnik.eodsql.spi.util.DataObjectBinding.BindingType;

/**
 * <p>
 * This is the class responsible for producing normal Java arrays
 * as results from query methods.
 * </p><p>
 * This class arguably makes selecting arrays from the database
 * faster than any of the other types available. The main reason
 * for this is that the {@code ArrayWrapper} class has 2 methods
 * for reading the {@code ResultSet}.
 * </p>
 *
 * <ol>
 *  <li>
 *      By preference, the {@code ArrayWrapper} will jump
 *      {@link ResultSet#last() to the end} of the {@code ResultSet},
 *      and then use the {@link ResultSet#getRow() row number}
 *      to pre-size the array. Once the array is allocated: the
 *      {@code ResultSet} is read {@link ResultSet#previous() backwards}.
 *  </li><li>
 *      If the {@code ResultSet} doesn't support the features required
 *      for this sort of reading, the {@code ArrayWrapper} class falls
 *      back to a "slow" mechanism: reading the {@code ResultSet} into
 *      an expanding array (similar to an {@code ArrayList}).
 *  </li>
 * </ol>
 *
 * <p>
 * Created on 2008/06/30
 * </p>
 * @author Jason Morris
 */
class ArrayWrapper extends AbstractResultSetWrapper<Object, Object> {

    /**
     * This field is set to {@literal true} if the driver only supports
     * forward-only {@code ResultSet} objects. We attempt several different
     * methods of loading a {@code ResultSet} in order to avoid
     * re-allocation of the array. This indicates that only the slowest
     * method is possible.
     */
    private boolean useSlowWrap = false;

    ArrayWrapper(final DataObjectBinding<Object> binding) {
        super(binding);
    }

    /**
     * This method uses an ArrayList to build up the array. This method basically assumes
     * a forward-only ResultSet (with the exception of possibly mosving beforeFirst()),
     * and is thus supported on databases that don't allow for {@code ResultSet.getRow()}.
     * 
     * @param results the {@code ResultSet} to wrap
     * @return the unwrapped array object
     */
    private Object slowWrap(final ResultSet results) throws SQLException {
        useSlowWrap = true;

        try {
            if(!results.isBeforeFirst() && !results.isFirst()) {
                try {
                    results.beforeFirst();
                } catch(final SQLException sqle) {
                    throw new EoDException(
                            "Something is very wrong with this ResultSet. " +
                            "There may be a bug in your JDBC Driver. " +
                            "The ResultSet does not support the " +
                            "beforeFirst() command, and is reporting a " +
                            "position that is not before " +
                            "or on the first row.", sqle);
                }
            }
        } catch(final SQLException sqle) {
            throw new EoDException(
                    "Something is very wrong with this " +
                    "ResultSet. There may be a bug in your JDBC Driver. " +
                    "The ResultSet cannot report whether it " +
                    "is before or on the first row.", sqle);
        }

        // this is a lot like an ArrayList but works with a typed
        // array instead of an array of Objects
        final ExpandingArray array = new ExpandingArray();

        if(!results.isBeforeFirst()) {
            if(results.isFirst()) {
                array.add(binding.unmarshall(results));
            } else {
                throw new EoDException("Something is very wrong with this " +
                        "ResultSet. There may be a bug in your JDBC Driver. " +
                        "The ResultSet should either be on or before the" +
                        " first row, but it is somewhere else.");
            }
        }

        while(results.next()) {
            array.add(binding.unmarshall(results));
        }

        return array.toArray();
    }

    @Override
    public Object wrap(final ResultSet results) throws SQLException {
        if(useSlowWrap) {
            return slowWrap(results);
        }

        int length = -1;

        try {
            results.setFetchDirection(ResultSet.FETCH_REVERSE);

            if(results.last()) {
                try {
                    length = results.getRow();
                } catch(final SQLException sqle) {
                    return slowWrap(results);
                }
            } else {
                // return an empty array, there are no rows in the ResultSet
                return Array.newInstance(binding.getObjectType(), 0);
            }
        } catch(final SQLException sqle) {
            return slowWrap(results);
        }

        final Object array = Array.newInstance(binding.getObjectType(), length);

        int index = length;

        do {
            Array.set(array, --index, binding.unmarshall(results));
        } while(results.previous());

        return array;
    }

    /**
     * We use this class as a sort of an ArrayList. This class maintains
     * an array of the type we are going to return from
     * {@link #wrap(java.sql.ResultSet)}. This class is used when the
     * {@code ResultSet} cannot be navigated backwards for compatibility
     * reasons. {@code ExpandingArray} is used in the
     * {@link #slowWrap(java.sql.ResultSet)} method.
     */
    private class ExpandingArray {

        private int index = 0;

        private int length = 10;

        private Object array = allocate(length);

        private Object allocate(final int size) {
            return Array.newInstance(binding.getObjectType(), size);
        }

        public void add(final Object value) {
            if(index == length) {
                int newCapacity = (length * 3) / 2 + 1;
                if(newCapacity < length + 1) {
                    newCapacity = length + 1;
                }

                final Object tmp = allocate(newCapacity);
                System.arraycopy(array, 0, tmp, 0, length);

                length = newCapacity;
                array = tmp;
            }

            Array.set(array, index++, value);
        }

        public Object toArray() {
            if(index < length) {
                final Object tmp = allocate(index);
                System.arraycopy(array, 0, tmp, 0, index);
                return tmp;
            } else {
                return array;
            }
        }

    }

    static final class Factory implements ResultSetWrapper.Factory {

        public boolean isTypeConstructable(
                final Type genericType,
                final Map<String, Object> parameters)
                throws InvalidDataTypeException {

            if(genericType instanceof Class) {
                final Class clazz = (Class)genericType;
                return clazz.isArray() && !clazz.getComponentType().isArray();
            }

            return false;
        }

        @SuppressWarnings("unchecked")
        public ResultSetWrapper create(
                final Type genericType,
                final Map<String, Object> parameters) {

            final BindingType bindingType =
                    parameters.containsKey(PARAMETER_BINDING_TYPE)
                    ? (BindingType)parameters.get(PARAMETER_BINDING_TYPE)
                    : BindingType.NORMAL_BINDING;

            final Class arrayType = (Class)genericType;

            if (parameters.containsKey(PARAMETER_CUSTOM_DATA_OBJECT_BINDING)) {
                final DataObjectBinding<Object> 
                    binding = (DataObjectBinding<Object>) parameters.get(PARAMETER_CUSTOM_DATA_OBJECT_BINDING);
                binding.setObjectType(arrayType.getComponentType());
                return new ArrayWrapper(binding);
            } else {
            return new ArrayWrapper(DataObjectBinding.getDataObjectBinding(
                    arrayType.getComponentType(), bindingType));
        }
        }

    }
}
