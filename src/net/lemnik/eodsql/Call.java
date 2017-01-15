package net.lemnik.eodsql;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import net.lemnik.eodsql.spi.util.NoDataObjectBinding;
import net.lemnik.eodsql.spi.util.DataObjectBinding;

/**
 * <p>
 * The {@code @Call} annotation is used to mark methods within a
 * {@link BaseQuery Query} interface that will execute a stored procedure.
 * The {@code @Call} annotation contains much the same data as a
 * {@link Select @Select} annotation.
 * </p><p>
 * A method annotated by {@code @Call} has the same conditions placed on it as
 * a method annotated by {@code @Select}. They may have the same return types,
 * delcare the same Exceptions, etc. Here is an example of how a {@code @Call} is used:
 * </p><p><code>
 * <span style="color: #0000ff;">public class</span> <b>User</b> {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<span style="color: #0000ff;">public</span> Integer id;<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<span style="color: #0000ff;">public</span> String username;<br>
 * }<br><br>
 *
 * <span style="color: #0000ff;">public interface</span> <b>UserQuery</b>
 *      <span style="color: #0000ff;">extends</span> BaseQuery {<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<span style="color: #00aa00; font-weight: bold;">@Call</span>(
 *      <span style="color: #ffa536">"call FIND_USER (?1)"</span)<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;<span style="color: #0000ff;">public</span> DataSet&lt;User&gt;
 *      <b>getUserById</b>(<span style="color: #0000ff;">int</span> id);<br>
 * }
 * </code></p><p>
 * Columns that cannot be mapped to fields or methods are ignored, as are fields and methods
 * that cannot be mapped to columns. {@code @Call} SQL uses the exact same query
 * parser as the {@link Select} annotation, and so can also accept complex types as
 * it's parameters, and insert their fields into the SQL query.
 * </p>
 * 
 * @author tsmith
 * @see Update
 * @see Select
 * @since 2.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Call {
    /**
     * The SQL query to execute on the database. The result set of the query will
     * be mapped to the datatype specified in the method declaration.
     * @see #call
     */
    String value() default "";

    /**
     * The SQL query to execute on the database. The result set of the query will
     * be mapped to the datatype specified in the method declaration. This value
     * works in the exact same way as {@link #value}, but is used when you wish
     * to specify values other than the SQL query.
     */
    String call() default "";

    /**
     * Specify whether or not the {@link DataSet} resulting from a call to the
     * annotated method will be connected to the database or not.
     *
     * @see DataSet
     * @see DataSet#disconnect
     */
    boolean disconnected() default false;

    /**
     * If <code>rubberstamp</code> is flagged as <code>true</code>, the annotated
     * method must return a {@link DataIterator}, otherwise it will be rejected by
     * the {@link QueryTool}. Rubber-stamping will cause the returned
     * <code>DataIterator</code> to use a single instance of the data-object for all
     * of the rows returned, instead of initializing a new one for each row. This can
     * be used to save CPU time and memory, and is especially useful in display code.
     */
    boolean rubberstamp() default false;

    /**
     * <p>
     * By deafult a {@link DataSet} returned by an <code>@Call</code> method is
     * read only. If you set this to <code>false</code> the returned <code>DataSet</code>
     * will implement the following additional methods:
     * </p>
     * <ul>
     *  <li>{@link java.util.List#add(Object)}</li>
     *  <li>{@link java.util.List#addAll(java.util.Collection)}</li>
     *  <li>{@link java.util.List#remove(int)}</li>
     *  <li>{@link java.util.List#set(int, Object)}</li>
     * </ul>
     * <p>
     * It is considered a validation error for a method to have readOnly set <code>false</code>
     * if it doesn't return a <code>DataSet</code>.
     * </p>
     *
     * @since 2.1
     */
    boolean readOnly() default true;

    /**
     *<p>
     * The cache class to be used by <code>DataSet</code>'s returned by the
     * annotated method. This only has an effect of methods that return a
     * {@link DataSet} type.
     *</p><p>
     * The default <code>DataSetCache</code> implementation is {@link ArrayDataSetCache},
     * which uses an array to cache the row values. For larger <code>ResultSet</code>'s, you
     * may want to look into a {@link NullDataSetCache} or using a {@link DataIterator} instead of
     * the <code>DataSet</code> class.
     *</p>
     */
    Class<? extends DataSetCache> cache() default ArrayDataSetCache.class;

    /**
     * <p>
     * A custom data object binding to be used on the result set of this query. The default value
     * {@link NoDataObjectBinding} indicates that no custom binding is to be used.
     * </p>
     * 
     * @since 2.2
     */
    Class<? extends DataObjectBinding> resultSetBinding() default NoDataObjectBinding.class;

    /**
     * <p>
     * The custom type bindings to be used on the parameters of this query. The binding is matched 
     * by position. The default type binding will be used for each parameter that has no  
     * <code>parameterBindings</code> entry or an entry that is of type {@link TypeMapper} itself. 
     * </p>
     * 
     * @since 2.2
     */
    Class<? extends TypeMapper>[] parameterBindings() default {};
}
