package net.lemnik.eodsql;

/**
 * The base class for {@code Exception}s in EoDSQL. This class is
 * also thrown from within many of the implementation methods, instead
 * of a {@link java.sql.SQLException}. If you want to catch EoDSQL specific
 * Exceptions, you may declare any of your query methods to throw
 * {@code EoDException}.
 * 
 * @author Jason Morris
 * @since 2.0
 */
public class EoDException extends RuntimeException {
    /**
     * Creates a new instance of {@code EoDException} without detail message.
     */
    public EoDException() {
    }

    /**
     * Constructs an instance of {@code EoDException} with the
     * specified detail message.
     * 
     * @param msg the detail message.
     */
    public EoDException(final String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@code EoDException} with the
     * specified cause.
     * 
     * @param cause the cause of this EoDException
     */
    public EoDException(final Throwable cause) {
        this(null, cause);
    }

    /**
     * Constructs an instance of {@code EoDException} with the specified
     * detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause of this {@code EoDException}.
     */
    public EoDException(final String message, final Throwable cause) {
        super(message);
        initCause(cause);
    }

}
