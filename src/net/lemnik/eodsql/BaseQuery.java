package net.lemnik.eodsql;

import java.sql.Connection;

/**
 * <p>
 * The {@code BaseQuery} is the super-interface for any interface wishing
 * to declare database queries. This validation is made by the {@link QueryTool} class
 * when the implementation of a query is {@link QueryTool#getQuery(java.lang.Class) requested}.
 * </p><p>
 * Methods within a query implementation (subclass of {@code BaseQuery})
 * may declare {@code throws SQLException}, in which case database
 * errors resulting from calls to that method will be thrown as normal
 * checked exceptions. If the method does not declare the exception, it
 * will be wrapped in a {@code RuntimeException} and thrown as an
 * unchecked exception.
 * </p><a name="pooling"><h2>Pooling vs. Non-Pooling</h2></a><p>
 * Depending on how a {@code BaseQuery} is fetched from the {@code QueryTool} determines how
 * {@code Connection}s will be handled by the implementation. A {@code BaseQuery} fetched with
 * a {@link javax.sql.DataSource DataSource} will not hold a {@code Connection}, but will instead
 * hold onto the {@code DataSource} object (note that a {@code BaseQuery} implementation returned
 * from {@link QueryTool#getQuery(java.lang.Class)} will hold a {@code DataSource} and not a
 * {@code Connection}). When a query method is invoked, the implementation
 * will fetch a {@code Connection} which will be used to perform the query.
 * </p><p>
 * A "pooling" {@code BaseQuery} implementation (one wrapping a {@code DataSource} instead of
 * a {@code Connection}) is safe to declare as a static variable. Pooling query implementations
 * are the recommended way to work, unless you have good reason not to use them (ie: your
 * database doesn't support pooling).
 * </p><p><code>
 * <span style="color: #0000ff;">public interface</span> <b>UserQuery</b>
 * <span style="color: #0000ff;">extends</span> BaseQuery {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<span style="color: #0000ff;">public static final</span>
 *      UserQuery INSTANCE = QueryTool.<b>getQuery</b>(UserQuery.<span style="
 *      color: #0000ff;">class</span>);<br><br>
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;<span style="color: #00aa00;
 *      font-weight: bold;">@Select</span>(<span style="color: #ffa536">"SELECT
 *      * FROM users WHERE id = ?1"</span>)<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<span style="color: #0000ff;">public</span> User
 *      <b>selectUserById</b>(<span style="color: #0000ff;">long</span> id);<br>
 * }
 * </code></p><p>
 * Non-Pooling {@code BaseQuery} objects wrap a single {@code Connection} object. This mechanism
 * allows you to work with the {@code Connection} outside of the {@code BaseQuery} object, but
 * is more expensive, since a {@code BaseQuery} instance must be constructed for each
 * {@code Connection} object. To release the {@code Connection} you will need to invoke the
 * {@link #close()} method (which will invoke {@link Connection#close()}.
 * </p>
 * 
 * @author Jason Morris
 *
 * @see QueryTool
 * @see QueryTool#getQuery(java.lang.Class)
 * @see Select @Select
 * @see Update @Update
 * @see Call @Call
 * @since 1.0
 */
public interface BaseQuery {
    /**
     * <p>
     * Closes this <code>BaseQuery</code>, and releases any database resources it is
     * currently holding. This method may throw an {@link java.sql.SQLException} (wrapped
     * in a {@code RuntimeException} if there is an underlying {@code Connection} object
     * which throws an {@code Exception} from it's {@link Connection#close() close()} method.
     * </p><p>
     * If this {@code BaseQuery} instance wraps a {@code DataSource} instead of a
     * {@code Connection} object, this method will make the {@code BaseQuery} instance
     * unusable. However, because <a href="#pooling">pooling</a> {@code BaseQuery} objects
     * hold no real state resources they can be declared as {@code static}.
     * </p>
     */
    void close();

    /**
     * <p>
     * Determines whether this <code>BaseQuery</code> implementation has been closed.
     * This method does <b>not</b> test to see whether the underlying {@code Connection}
     * object (if there is one) has been closed. Thus the following code will return false:
     * </p><p><code>
     * <span style="color: #0000ff;">final</span> Connection connection =
     *      DriverManager.<b>getConnection</b>(databaseURL);<br>
     * <span style="color: #0000ff;">final</span> UserQuery query =
     *      QueryTool.<b>getQuery</b>(connection);<br><br>
     *
     * <span style="color: #999999;">// do some work with the query here</span><br><br>
     *
     * connection.<b>close</b>();<br><br>
     *
     * <span style="color: #0000ff;">return</span> query.<b>isClosed</b>();
     * </code></p><p>
     * Although the {@code Connection} object has been closed, the {@code UserQuery} object
     * remains valid (since it has not yet been closed). Thus the exact contract of this
     * method is that it will return {@literal true} only when {@link #close()} has been
     * invoked on the same object.
     * </p>
     *
     * @return {@literal true} if this {@code BaseQuery} has been closed
     */
    boolean isClosed();

}
