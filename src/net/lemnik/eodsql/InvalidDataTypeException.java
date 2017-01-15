package net.lemnik.eodsql;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * This method is thrown by the {@link QueryTool} class when a data-type
 * returned by an {@link Select @Select} method is found to be invalid.
 * 
 * @author Jason Morris
 */
public class InvalidDataTypeException extends InvalidQueryException {
    private Type type;

    /**
     * Creates a new instance of {@code InvalidDataTypeException}
     * without detail message. The detail message of the new
     * {@code Exception} will be based on the class name of the
     * type given.
     *
     * @param type the offending data-type
     */
    public InvalidDataTypeException(final Class<?> type) {
        super("dataType=" + type != null
                ? type.getName()
                : "<null>");
        
        this.type = type;
    }
    
    /**
     * Constructs an instance of {@code InvalidDataTypeException}
     * with the specified detail message and invalid type.
     * 
     * @param msg the detail message.
     * @param type the offending data-type
     */
    public InvalidDataTypeException(final String msg, final Type type) {
        super(msg + " [dataType=" + type + "]");
        this.type = type;
    }

    /**
     * Constructs an instance of <code>InvalidDataTypeException</code>
     * with the specified detail message and invalid type.
     *
     * @param msg the detail message.
     * @param type the offending data-type
     */
    public InvalidDataTypeException(final String msg, final Class<?> type) {
        super(msg + " [dataType=" + (type != null
                ? type.getName()
                : "<null>") + "]");
        
        this.type = type;
    }

    /**
     * Constructs an instance of {@code InvalidDataTypeException}
     * with the specified detail message, and a specific method that
     * caused the problem.
     * 
     * @param msg the detail message.
     * @param type the offending data-type
     * @param problem the method that caused the exception to be thrown
     */
    public InvalidDataTypeException(
            final String msg,
            final Class<?> type,
            final Method problem) {

        super(msg + " [dataType=" + (type != null
                ? type.getName()
                : "<null>") + "]", problem);
        
        this.type = type;
    }

    /**
     * Returns the {@code Class} of the data-type that caused this exception
     * to be thrown. If the offending type was not a {@code Class},
     * this method will return {@literal null}.
     * 
     * @return the offending data-type
     */
    public Class<?> getDataType() {
        return type instanceof Class ? (Class)type : null;
    }
    
    /**
     * Returns the full data-type that  caused this exception
     * to be thrown.
     * 
     * @return the offending data-type
     */
    public Type getGenericDataType() {
        return type;
    }

}
