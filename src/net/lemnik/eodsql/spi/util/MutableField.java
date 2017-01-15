package net.lemnik.eodsql.spi.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import net.lemnik.eodsql.EoDException;

/**
 * Created on 2008/06/13
 * @author Jason Morris
 */
class MutableField implements MutableColumn {
    private final String columnName;

    private final Field field;
    
    MutableField(
            final String columnName,
            final Field field,
            final boolean setAccessibleFlag) {

        this.columnName = columnName;
        this.field = field;
        
        if(!Modifier.isPublic(field.getModifiers()) && setAccessibleFlag) {
            field.setAccessible(true);
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public void set(
            final Object object,
            final Object value)
            throws EoDException {

        try {
            field.set(object, value);
        } catch(final IllegalArgumentException argumentException) {
            throw new EoDException("Cannot assign value to field: " + this,
                    argumentException);
        } catch(final IllegalAccessException accessException) {
            throw new EoDException("Cannot assign value to field: " + this,
                    accessException);
        }
    }

    public Object get(final Object object) throws EoDException {
        try {
            return field.get(object);
        } catch(final IllegalArgumentException argumentException) {
            throw new EoDException("Cannot get value from field: " + this,
                    argumentException);
        } catch(final IllegalAccessException accessException) {
            throw new EoDException("Cannot get value from field: " + this,
                    accessException);
        }
    }

    public Class getType() {
        return field.getType();
    }
    
    @Override
    public String toString() {
        return field.getDeclaringClass().getName() + "." + field.getName();
    }

}
