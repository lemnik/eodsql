package net.lemnik.eodsql.spi.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import net.lemnik.eodsql.EoDException;

/**
 * Created on 2008/06/13
 * @author Jason Morris
 */
class MutableProperty implements MutableColumn {
    private final String columnName;

    private final Method setter;

    private final Method getter;
    
    private final Class type;

    MutableProperty(
            final String columnName,
            final Method setter,
            final Method getter,
            final Class type,
            final boolean setAccessibleFlag) {
        
        this.columnName = columnName;
        this.setter = setter;
        this.getter = getter;
        this.type = type;
        
        if(setter != null &&
                !Modifier.isPublic(setter.getModifiers()) &&
                setAccessibleFlag) {

            setter.setAccessible(true);
        }
        
        if(getter != null &&
                !Modifier.isPublic(getter.getModifiers()) &&
                setAccessibleFlag) {
            
            getter.setAccessible(true);
        }
    }

    public String getColumnName() {
        return columnName;
    }

    public void set(final Object object, final Object value) {
        try {
            setter.invoke(object, value);
        } catch(final IllegalAccessException iae) {
            throw new EoDException(iae);
        } catch(final IllegalArgumentException iae) {
            throw new EoDException(iae);
        } catch(final InvocationTargetException ite) {
            throw new EoDException(ite);
        }
    }

    public Object get(final Object object) {
        try {
            return getter.invoke(object);
        } catch(final IllegalAccessException iae) {
            throw new EoDException(iae);
        } catch(final IllegalArgumentException iae) {
            throw new EoDException(iae);
        } catch(final InvocationTargetException ite) {
            throw new EoDException(ite);
        }
    }

    public Class getType() {
        return type;
    }

}
