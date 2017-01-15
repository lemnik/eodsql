package net.lemnik.eodsql.spi.util;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.lemnik.eodsql.EoDException;

/**
 * A {@link DataObjectBinding} that stores the {@link ResultSet} in a
 * {@link HashMap}.
 * 
 * @author Bernd Rinn
 */
public class MapDataObjectBinding extends DataObjectBinding<Map<String, Object>> {
    @Override
    public Class<Map<String, Object>> getObjectType() {
	return getStringObjectMapObjectType();
    }

    @SuppressWarnings("unchecked")
    public static Class<Map<String, Object>> getStringObjectMapObjectType() {
	return (Class<Map<String, Object>>) Collections.<String, Object> emptyMap().getClass();
    }

    @Override
    public Map<String, Object> newInstance() throws EoDException {
	return new HashMap<String, Object>();
    }

    @Override
    public void marshall(Map<String, Object> from, ResultSet results)
	    throws SQLException, EoDException {
	final ResultSetMetaData metaData = results.getMetaData();
	for (Map.Entry<String, Object> entry : from.entrySet()) {
	    final int i = results.findColumn(entry.getKey());
	    final int type = metaData.getColumnType(i);
	    if (entry.getValue() == null) {
		results.updateNull(i);
		continue;
	    }
	    switch (type) {
	    case Types.BIT:
	    case Types.BOOLEAN:
		results.updateBoolean(i, (Boolean) entry.getValue());
		break;
	    case Types.CHAR:
	    case Types.VARCHAR:
	    case Types.LONGVARCHAR:
		results.updateString(i, entry.getValue().toString());
		break;
	    case Types.DATE:
		results.updateDate(i, new Date(((java.util.Date) entry
			.getValue()).getTime()));
		break;
	    case Types.TIME:
		results.updateTime(i, new Time(((java.util.Date) entry
			.getValue()).getTime()));
		break;
	    case Types.TIMESTAMP:
		results.updateTimestamp(i, new Timestamp(
			((java.util.Date) entry.getValue()).getTime()));
		break;
	    case Types.DECIMAL:
	    case Types.NUMERIC:
		results.updateBigDecimal(i, (BigDecimal) entry.getValue());
		break;
	    case Types.REAL:
		results.updateFloat(i, (Float) entry.getValue());
		break;
	    case Types.DOUBLE:
	    case Types.FLOAT:
		results.updateDouble(i, (Double) entry.getValue());
		break;
	    case Types.TINYINT:
		results.updateByte(i, (Byte) entry.getValue());
		break;
	    case Types.SMALLINT:
		results.updateShort(i, (Short) entry.getValue());
		break;
	    case Types.INTEGER:
		results.updateInt(i, (Integer) entry.getValue());
		break;
	    case Types.BIGINT:
		results.updateLong(i, (Long) entry.getValue());
		break;
	    case Types.BINARY:
	    case Types.VARBINARY:
	    case Types.LONGVARBINARY:
		results.updateBytes(i, (byte[]) entry.getValue());
		break;
	    case Types.JAVA_OBJECT:
		results.updateObject(i, entry.getValue());
		break;
	    case Types.ARRAY:
		results.updateArray(i, (Array) entry.getValue());
		break;
	    }
	}
    }

    @Override
    public void unmarshall(ResultSet row, Map<String, Object> into)
	    throws SQLException, EoDException {
	final ResultSetMetaData metaData = row.getMetaData();
	for (int i = 1; i <= metaData.getColumnCount(); ++i) {
	    final int type = metaData.getColumnType(i);
	    final String name = metaData.getColumnName(i);
	    switch (type) {
	    case Types.BIT:
	    case Types.BOOLEAN:
		into.put(name, row.getBoolean(i));
		break;
	    case Types.CHAR:
	    case Types.VARCHAR:
	    case Types.LONGVARCHAR:
		into.put(name, row.getString(i));
		break;
	    case Types.DATE:
		into.put(name, row.getDate(i));
		break;
	    case Types.TIME:
		into.put(name, row.getTime(i));
		break;
	    case Types.TIMESTAMP:
		into.put(name, row.getTimestamp(i));
		break;
	    case Types.DECIMAL:
	    case Types.NUMERIC:
		into.put(name, row.getBigDecimal(i));
		break;
	    case Types.REAL:
		into.put(name, row.getFloat(i));
		break;
	    case Types.DOUBLE:
	    case Types.FLOAT:
		into.put(name, row.getDouble(i));
		break;
	    case Types.TINYINT:
		into.put(name, row.getByte(i));
		break;
	    case Types.SMALLINT:
		into.put(name, row.getShort(i));
		break;
	    case Types.INTEGER:
		into.put(name, row.getInt(i));
		break;
	    case Types.BIGINT:
		into.put(name, row.getLong(i));
		break;
	    case Types.BINARY:
	    case Types.VARBINARY:
	    case Types.LONGVARBINARY:
		into.put(name, row.getBytes(i));
		break;
	    case Types.JAVA_OBJECT:
		into.put(name, row.getObject(i));
		break;
	    case Types.ARRAY:
		into.put(name, row.getArray(i));
		break;
	    }
	    if (row.wasNull()) {
		into.put(name, null);
	    }
	}
    }
}
