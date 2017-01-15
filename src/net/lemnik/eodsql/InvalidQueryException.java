package net.lemnik.eodsql;

import java.lang.reflect.Method;

/**
 * This exception is only ever thrown by methods in the {@link QueryTool}
 * class when an implementation of {@link BaseQuery} is requested which
 * violates any of the rule's for a query class.
 *
 * @author Jason Morris
 */
public class InvalidQueryException extends RuntimeException {

    /**
     * Generally used to flag which method in a Query interface is
     * invalid. However in some cases the problem may be with the
     * interface itself (and not a particular method), in which case
     * this will be {@literal null}.
     */
    private final Method problem;

    /**
     * Creates a new instance of {@code InvalidQueryException}
     * without detail message.
     */
    public InvalidQueryException() {
        this.problem = null;
    }

    /**
     * Constructs an instance of {@code InvalidQueryException}
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public InvalidQueryException(final String msg) {
        super(msg);
        this.problem = null;
    }

    /**
     * Constructs an instance of {@code InvalidQueryException}
     * with the specified detail message, and a specific method that
     * caused the problem.
     *
     * @param msg the detail message.
     * @param problem the method that caused the exception to be thrown
     */
    public InvalidQueryException(final String msg, final Method problem) {
        super(msg);
        this.problem = problem;
    }

    /**
     * Constructs an instance of {@code InvalidQueryException}
     * with the specified detail message and cause.
     *
     * @param msg the detail message.
     * @param cause the error the caused this {@code InvalidQueryException}
     *      to be thrown
     */
    public InvalidQueryException(final String msg, final Throwable cause) {
        super(msg, cause);
        this.problem = null;
    }

    /**
     * Constructs an instance of {@code InvalidQueryException}
     * with the specified detail message, and a specific method that
     * caused the problem. This constructor also allows for a Throwable
     * cause to be attached.
     * 
     * @param msg the detail message.
     * @param problem the method that caused the exception to be thrown
     * @param cause the Throwable that is being wrapped
     */
    public InvalidQueryException(
            final String msg,
            final Method problem,
            final Throwable cause) {

        super(msg, cause);
        this.problem = problem;
    }

    /**
     * Generally the reason a query interface is found to be invalid
     * is that one of it's methods have been badly annotated.
     * If this is the case, a call to {@code getInvalidMethod} will
     * return the offending method, otherwise it will return {@literal null}.
     * 
     * @return the method that caused this exception to be thrown
     */
    public Method getInvalidMethod() {
        return problem;
    }

    /**
     * Much like the default {@link Throwable#toString()} method, this
     * method returns the default description followed by an additonal
     * line with the {@link Method#toGenericString() generic string}
     * of the problem method.
     *
     * @return a detailed description of the invalid query error
     */
    @Override
    public String toString() {
        final Method method = getInvalidMethod();

        if(method != null) {
            return super.toString() +
                    System.getProperty("line.separator") +
                    method.toGenericString();
        } else {
            return super.toString();
        }
    }

}
