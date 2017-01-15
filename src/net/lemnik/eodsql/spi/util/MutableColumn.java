package net.lemnik.eodsql.spi.util;

import net.lemnik.eodsql.EoDException;

/**
 * Represents a field or getter/setter method pair that can be bound to a column
 * in a result set.
 *
 * @author Jason Morris
 */
interface MutableColumn {

    String getColumnName();

    void set(Object object, Object value) throws EoDException;

    Object get(Object object) throws EoDException;

    Class getType();

}
