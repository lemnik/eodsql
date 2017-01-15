package net.lemnik.eodsql;

import java.util.Iterator;

/**
 * <p>
 * A {@code DataIterator} is the forward only version of {@link DataSet}.
 * The {@code DataIterator} interfaces has two default implementations:
 * </p>
 * <ol>
 *    <li>
 *        rubber-stamping mode - the same object is returned for all the rows,
 *        and modified between calls.
 *    </li>
 *    <li>
 *        normal operation - a new object is constructed for each row
 *    </li>
 * </ol>
 * <p>
 *    Unlink {@link DataSet}, a {@code DataIterator} is always connected to
 *    the database, and will hold the database resources until it is closed,
 *    <i>or</i> until the end of the results are reached. If you want a
 *    disconnected {@code Iterator}, you can return a disconnected
 *    {@link DataSet}. A method that returns a {@code DataIterator} and
 *    is annotation as disconnected will cause validation to fail
 *    in the {@link QueryTool}.
 * </p><p>
 *    Although {@code DataIterator} extends from the
 *    {@link java.util.Iterator} interface, it also inherits from the
 *    {@link java.lang.Iterable} interface, to make it compatible with the
 *    Java 5 foreach statement. The {@code iterator()} method
 *    will simply return {@literal this}.
 * </p>
 * <h2>Rubberstamping Mode</h2><p>
 * When a very large number of rows needs to be read, and rendered to a destination,
 * rubberstamping mode can greatly enhance performance. Instead of creating a new
 * {@code E} object for each row returned, a rubberstamping {@code DataIterator}
 * creates one object when the first row is requested and modifies it each time
 * {@link #next()} is invoked.
 * </p><p>
 * <code>
 * <span style="color: #0000ff;">final</span> DataIterator&lt;User&gt; users =
 * queryObject.<b>selectAllUsers</b>();<br>
 * <span style="color: #0000ff;">final</span> PrintWriter output =
 * response.<b>getWriter</b>();<br><br>
 * User previousUser = <span style="color: #0000ff;">null</span>;<br><br>
 *
 * <span style="color: #0000ff;">for</span>(<span style="color: #0000ff;">final</span> User user : users) {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;out.<b>print</b>(<span style="color: #ffa536">"&lt;tr&gt;&lt;td&gt;"</span>);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;out.<b>print</b>(user.<b>getUsername</b>());<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;out.<b>print</b>(<span style="color: #ffa536">"&lt;/td&gt;&lt;td&gt;"</span>);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;out.<b>print</b>(previousUser != <span style="color: #0000ff;">null</span> ? previousUser == user : <span style="color: #ffa536">"n/a"</span>);<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;out.<b>print</b>(<span style="color: #ffa536">"&lt;/td&gt;&lt;/tr&gt;"</span>);<br><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;previousUser = user;<br>
 * }
 * </code></p><p>
 * Given any number of {@code User} objects in the above code, a rubberstamping
 * {@code DataIterator} will act exactly the same as a normal {@code DataIterator} except
 * that the second column will contain {@literal "true"}, where a normal {@code DataIterator}
 * will contain {@literal "false"}.
 * </p>
 *
 * @author Jason Morris
 * @see Select#rubberstamp()
 * @see DataSet
 */
public interface DataIterator<E> extends Iterator<E>, Iterable<E> {
    /**
     * <p>
     * Requests that this <code>DataIterator</code> disconnect from the database and
     * become invalid. Once this method has been called the object will act as though
     * it has come to the end of it's list of elements, and thus throw
     * {@code NoSuchElementException}s in it's {@link Iterator#next()} method.
     * </p><p>
     * When a {@code DataIterator} comes to the end of it's {@code ResultSet}, it
     * will automatically invoke this method. Thus when it is used in a normal manor
     * this method does not need to be invoked.
     * </p>
     */
    void close();

    /**
     * Returns whether or not this <code>DataIterator</code> has had it's
     * {@link #close} method called yet. This method will also return {@literal true} if
     * the {@code DataIterator} has come to the end of it's {@code ResultSet}.
     *
     * @return <code>true</code> if the <code>close()</code> method has already been called
     * on this <code>DataIterator</code>
     * @see #close()
     */
    boolean isClosed();

}
