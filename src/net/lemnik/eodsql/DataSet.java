package net.lemnik.eodsql;

import java.util.List;

/**
 * <p>
 * A <code>DataSet</code> allows access to objects mapped from a
 * {@link Select @Select}, or {@link Call @Call} query. A <code>DataSet</code>
 * implementation will include all of the required methods required by
 * the <code>List</code> interface, optional methods may or may not be implemented,
 * as the <code>DataSet</code> may be disconnected from the database or read-only.
 * </p><p>
 * A <code>DataSet</code> that was requested as
 * {@link Select#disconnected() connected} will lazy-load objects from the
 * database, and remains connected until it's {@link #close()} method is
 * called. A connected <code>DataSet</code> will use a {@link DataSetCache cache} of the
 * objects, and so does not reflect changes in the database, for
 * this a new request must be made, or a {@link NullDataSetCache} set in the
 * query {@link Select#cache() annotation}. A connected <code>DataSet</code>
 * may at any time disconnect from the database, and cache the entire
 * ResultSet in memory. This is typically done when a method is called
 * that requires the entire ResultSet (such as {@link java.util.List#toArray()}).
 * </p><p>
 * Exceptions resulting from calls to a <code>DataSet</code> object will be
 * wrapped in a <code>RuntimeException</code> and be re-thrown. It is advised
 * that <code>DataSet</code> access is wrapped in a try-catch block to avoid
 * getting database errors where you don't want them. Errors are less likely
 * to occur from disconnected <code>DataSet</code>s.
 * </p><h2>DataSet and Eager JDBC Drivers</h2><p>
 * Some JDBC Drivers (such as MySQL-Connector) pre-load all or large portions of
 * the {@code ResultSet} as soon as the query is executed. With these drivers it
 * is often faster to have a query method return an array or {@link DataIterator}
 * instead of a {@code DataSet} object.
 * </p>
 * 
 * @author Jason Morris
 */
public interface DataSet<T> extends List<T> {
    /**
     * Requests that this <code>DataSet</code> release any database resources that
     * it currently holds, such as {@link java.sql.ResultSet}'s, and 
     * {@link java.sql.Statement}'s. Once this method has been called, attempting
     * to call accessor methods on the <code>DataSet</code> may result in a
     * <code>RuntimeException</code> (wrapping an <code>SQLException</code>).
     *
     * @see #isConnected()
     * @see #disconnect()
     */
    void close();

    /**
     * Checks to see if this <code>DataSet</code> is currently connected to the
     * database. If {@link #disconnect} or {@link #close} has been called, this will
     * return {@literal false}. It may also return {@literal false} if the
     * <code>DataSet</code> has disconnected for other reasons. If the
     * <code>DataSet</code> was requested as disconnected, this will always return
     * {@literal false}.
     * @return {@literal true} if this <code>DataSet</code> is holding database resources
     */
    boolean isConnected();

    /**
     * <p>
     * Requests that this <code>DataSet</code> disconnect from the database cleanly,
     * but still maintain it's functionality. If this method is called on a
     * <code>DataSet</code> was requested as {@link Select#disconnected disconnected}
     * then this method will have no effect. Otherwise, if the <code>DataSet</code>
     * is currently connected with the database it will copy the remaining results
     * into an internal cache, and then {@link #close} itself.
     * </p><p>
     * This method will ensure the {@code DataSet} is valid even if it's selected
     * {@link DataSetCache cache} implementation does not guarentee reachabilty. The
     * {@code DataSet} implementation actually swaps the selected cache for a specialized
     * {@code DataSetCache} implementation. Population of the new cache will first look
     * at the selected {@code DataSetCache} before fetching the row from the database.
     * </p>
     *
     * @see #isConnected()
     * @see Select#disconnected()
     * @see Call#disconnected()
     */
    void disconnect();

}