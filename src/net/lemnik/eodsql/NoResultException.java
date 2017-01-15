package net.lemnik.eodsql;

/**
 * Thrown when you attempt to {@code @Select(into)} and there is no
 * result found in the database.
 *
 * @author Jason Morris
 */
public class NoResultException extends EoDException {

    public NoResultException() {
    }

    public NoResultException(final String msg) {
        super(msg);
    }

    public NoResultException(final Throwable cause) {
        super(cause);
    }

    public NoResultException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
