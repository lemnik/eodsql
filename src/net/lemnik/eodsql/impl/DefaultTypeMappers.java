package net.lemnik.eodsql.impl;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.sql.Types;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.Map;
import java.util.UUID;
import java.util.Date;

import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TypeMapper;

/**
 * @author Jason Morris
 */
class DefaultTypeMappers {

    static void register() {
        final Map<Class, TypeMapper> typeMap = QueryTool.getTypeMap();

        typeMap.put(Byte.class, new ByteMapper());
        typeMap.put(Byte.TYPE, new ByteMapper());
        typeMap.put(Short.class, new ShortMapper());
        typeMap.put(Short.TYPE, new ShortMapper());
        typeMap.put(Integer.class, new IntegerMapper());
        typeMap.put(Integer.TYPE, new IntegerMapper());
        typeMap.put(Long.class, new LongMapper());
        typeMap.put(Long.TYPE, new LongMapper());
        typeMap.put(Float.class, new FloatMapper());
        typeMap.put(Float.TYPE, new FloatMapper());
        typeMap.put(Double.class, new DoubleMapper());
        typeMap.put(Double.TYPE, new DoubleMapper());
        typeMap.put(Boolean.class, new BooleanMapper());
        typeMap.put(Boolean.TYPE, new BooleanMapper());
        typeMap.put(Character.class, new CharacterMapper());
        typeMap.put(Character.TYPE, new CharacterMapper());
        typeMap.put(String.class, new StringMapper());
        typeMap.put(UUID.class, new UUIDMapper());
        typeMap.put(Date.class, new DateMapper());
        typeMap.put(BigDecimal.class, new BigDecimalMapper());
        typeMap.put(BigInteger.class, new BigIntegerMapper());
        typeMap.put(byte[].class, new ByteArrayMapper());
    }

    private DefaultTypeMappers() {
    }

    static class ByteMapper implements TypeMapper<Byte> {

        public Byte get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return Byte.valueOf(results.getByte(column));
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final Byte obj)
                throws SQLException {

            if(obj != null) {
                statement.setByte(column, obj.byteValue());
            } else {
                statement.setNull(column, Types.TINYINT);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final Byte obj)
                throws SQLException {

            results.updateByte(column, obj);
        }

    }

    static class ShortMapper implements TypeMapper<Short> {

        public Short get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return Short.valueOf(results.getShort(column));
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final Short obj)
                throws SQLException {

            if(obj != null) {
                statement.setShort(column, obj.shortValue());
            } else {
                statement.setNull(column, Types.SMALLINT);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final Short obj)
                throws SQLException {

            results.updateShort(column, obj);
        }

    }

    static class IntegerMapper implements TypeMapper<Integer> {

        public Integer get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return Integer.valueOf(results.getInt(column));
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final Integer obj)
                throws SQLException {

            if(obj != null) {
                statement.setInt(column, obj.intValue());
            } else {
                statement.setNull(column, Types.INTEGER);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final Integer obj)
                throws SQLException {

            results.updateInt(column, obj);
        }

    }

    static class LongMapper implements TypeMapper<Long> {

        public Long get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return Long.valueOf(results.getLong(column));
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final Long obj)
                throws SQLException {

            if(obj != null) {
                statement.setLong(column, obj.longValue());
            } else {
                statement.setNull(column, Types.BIGINT);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final Long obj)
                throws SQLException {

            results.updateLong(column, obj);
        }

    }

    static class FloatMapper implements TypeMapper<Float> {

        public Float get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return Float.valueOf(results.getFloat(column));
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final Float obj)
                throws SQLException {

            if(obj != null) {
                statement.setFloat(column, obj.floatValue());
            } else {
                statement.setNull(column, Types.FLOAT);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final Float obj)
                throws SQLException {

            results.updateFloat(column, obj);
        }

    }

    static class DoubleMapper implements TypeMapper<Double> {

        public Double get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return Double.valueOf(results.getDouble(column));
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final Double obj)
                throws SQLException {

            if(obj != null) {
                statement.setDouble(column, obj.doubleValue());
            } else {
                statement.setNull(column, Types.DOUBLE);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final Double obj)
                throws SQLException {

            results.updateDouble(column, obj);
        }

    }

    static class BooleanMapper implements TypeMapper<Boolean> {

        public Boolean get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return Boolean.valueOf(results.getBoolean(column));
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final Boolean obj)
                throws SQLException {

            if(obj != null) {
                statement.setBoolean(column, obj.booleanValue());
            } else {
                statement.setNull(column, Types.BOOLEAN);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final Boolean obj)
                throws SQLException {

            results.updateBoolean(column, obj);
        }

    }

    static class CharacterMapper implements TypeMapper<Character> {

        public Character get(
                final ResultSet results,
                final int column)
                throws SQLException {

            String value = results.getString(column);

            if(value != null) {
                return Character.valueOf(value.charAt(0));
            } else {
                return null;
            }
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final Character obj)
                throws SQLException {

            if(obj != null) {
                statement.setString(column, obj.toString());
            } else {
                statement.setNull(column, Types.CHAR);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final Character obj)
                throws SQLException {

            results.updateString(column, obj.toString());
        }

    }

    static class StringMapper implements TypeMapper<String> {

        public String get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return results.getString(column);
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final String obj)
                throws SQLException {

            if(obj != null) {
                statement.setString(column, obj);
            } else {
                statement.setNull(column, Types.VARCHAR);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final String obj)
                throws SQLException {

            results.updateString(column, obj);
        }

    }

    static class UUIDMapper implements TypeMapper<UUID> {

        public UUID get(
                final ResultSet results,
                final int column)
                throws SQLException {

            String value = results.getString(column);

            if(value != null) {
                return UUID.fromString(value);
            } else {
                return null;
            }
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final UUID obj)
                throws SQLException {

            if(obj != null) {
                statement.setString(column, obj.toString());
            } else {
                statement.setNull(column, Types.VARCHAR);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final UUID obj)
                throws SQLException {

            results.updateString(column, obj.toString());
        }

    }

    static class DateMapper implements TypeMapper<Date> {

        public Date get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return (Date)results.getObject(column);
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final Date obj)
                throws SQLException {

            if(obj != null) {
                // try high precision first
                long time = obj.getTime();
                try {
                    statement.setTimestamp(column, new java.sql.Timestamp(time));
                } catch(SQLException sqle) {
                    try {
                        statement.setDate(column, new java.sql.Date(time));
                    } catch(SQLException ex) {
                        statement.setObject(column, obj);
                    }
                }
            } else {
                statement.setNull(column, Types.TIMESTAMP);
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final Date obj)
                throws SQLException {

            results.updateTimestamp(column,
                    new java.sql.Timestamp(obj.getTime()));
        }

    }

    static class BigDecimalMapper implements TypeMapper<BigDecimal> {

        public BigDecimal get(
                final ResultSet results,
                final int column)
                throws SQLException {

            try {
                return results.getBigDecimal(column);
            } catch(SQLException sqle) {
                final Object object = results.getObject(column);

                if(object instanceof Float || object instanceof Double) {
                    return new BigDecimal(((Number)object).doubleValue());
                } else if(object instanceof Number) {
                    return new BigDecimal(((Number)object).longValue());
                } else if(object instanceof String) {
                    return new BigDecimal((String)object);
                }

                throw sqle;
            }
        }

        public void set(
                final ResultSet results,
                final int column,
                final BigDecimal obj)
                throws SQLException {

            results.updateBigDecimal(column, obj);
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final BigDecimal obj)
                throws SQLException {

            statement.setBigDecimal(column, obj);
        }

    }

    static class BigIntegerMapper implements TypeMapper<BigInteger> {

        public BigInteger get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return new BigInteger(results.getBytes(column));
        }

        public void set(
                final ResultSet results,
                final int column,
                final BigInteger obj)
                throws SQLException {

            results.updateBytes(column, obj.toByteArray());
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final BigInteger obj)
                throws SQLException {

            if(obj != null) {
                statement.setBytes(column, obj.toByteArray());
            } else {
                statement.setNull(column, Types.VARBINARY);
            }
        }

    }

    static class ByteArrayMapper implements TypeMapper<byte[]> {

        public byte[] get(
                final ResultSet results,
                final int column)
                throws SQLException {

            return results.getBytes(column);
        }

        public void set(
                final ResultSet results,
                final int column,
                final byte[] obj)
                throws SQLException {

            results.updateBytes(column, obj);
        }

        public void set(
                final PreparedStatement statement,
                final int column,
                final byte[] obj)
                throws SQLException {

            if(obj != null) {
                statement.setBytes(column, obj);
            } else {
                statement.setNull(column, Types.VARBINARY);
            }
        }

    }
}
