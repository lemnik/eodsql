package net.lemnik.eodsql;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import java.sql.ResultSet;
import java.sql.Statement;

import net.lemnik.eodsql.spi.MethodImplementation;
import net.lemnik.eodsql.spi.util.NoDataObjectBinding;
import net.lemnik.eodsql.spi.util.DataObjectBinding;

/**
 * <p>
 * The <code>@Select</code> annotation is used to mark methods within a
 * {@link BaseQuery Query} class that will return some form of data from
 * database (typically a {@literal "SELECT"} query. The {@code Select}
 * annotation contains the SQL query that will be executed on the database
 * </p><p>
 * Here is an example of how a <code>@Select</code> is used:
 * 
 * <pre>
 *     public class User {
 *         public Integer id;
 *         public String username;
 *     }
 *     
 *     public interface UserQuery extends BaseQuery {
 *         <span style="color: #00f;">@Select("SELECT * FROM user WHERE id = ?1")</span>
 *         public DataSet&lt;User&gt; getUserById(int id);
 *     }
 * </pre>
 * 
 * Any method annotated with <code>@Select</code> may return any one of the
 * following datatypes:
 * 
 * <ul>
 *     <li>
 *         the exact class to map - this will either return the first result
 *         found, or <code>null</code> if no results where returned.
 *     </li><li>
 *         the class wrapped in a {@link DataSet} which can be declared as
 *         either connected (default) or {@link #disconnected() disconnected}
 *     </li><li>
 *         the data object wrapped in a {@link DataIterator}, which may
 *         be declared to {@link #rubberstamp()} (and thus only allocate
 *         one instance of the contained data object)
 *     </li><li>
 *         an array of the class to bind to
 *     </li><li>
 *         the class wrapped in a {@link java.util.Set Set},
 *         {@link java.util.List List} or
 *         {@link java.util.Collection Collection} - these types have no
 *         connection to the database, the data is loaded into them and
 *         the result is returned.
 *     </li><li>
 *         a concrete implementation of a {@code Collection} class
 *         (must include a generic declaration of the contained type -
 *         {@code Vector<User>} is ok, but {@code MyUserCollection} is not
 *     </li><li>
 *         {@link QueryTool#getTypeMap() primitive types} can also be
 *         mapped either as single objects, or as the contents of any
 *         of the above structures
 *     </li>
 * </ul>
 * </p><p>
 * Columns that cannot be mapped to fields are ignored, as are fields
 * that cannot be mapped to columns. <code>@Select</code> SQL uses the
 * exact same query parser as the {@link Update} annotation, and so can
 * also accept complex types as it's parameters, and insert their
 * fields into the SQL query.
 * </p>
 * @author Jason Morris
 * @see Update @Update
 * @see Call @Call
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {

    /**
     * The SQL query to execute on the database. The result set of
     * the query will be mapped to the datatype specified in the
     * method declaration. This attribute is used for convenience
     * when none of the other attributes need to be explicidly set.
     * 
     * @see #sql
     * @return the EoD SQL query to execute against the database
     */
    String value() default "";

    /**
     * <p>
     * The SQL query to execute on the database. The result set of the
     * query will be mapped to the datatype specified in the method
     * declaration. This attribute works in the exact same way as
     * {@link #value}, but is used when you wish to specify values
     * other than the SQL query.
     * </p><p>
     * The SQL is partly parsed by the implementation, allowing for
     * parameters of the annotated method to be used in the SQL
     * statement. Selecting a specific {@code User} object from the
     * database may look as follows:
     * </p>
     * <code>
     * <span style="color: #00f;">@Select("SELECT * FROM user WHERE id = ?1")</span><br>
     * User selectUserById(int id) throws SQLException;
     * </code>
     * <p>
     * More complex introspection of the method parameters can be done
     * using the bracketed syntax as follows:
     * </p>
     * <code>
     * <span style="color: #00f;">@Select("SELECT * FROM user WHERE email = ?{1.email}")</span><br>
     * DataIterator&lt;User&gt; selectUsersByUserEmail(User newUser) throws SQLException;
     * </code>
     *
     * @return the SQL query to execute against the database
     */
    String sql() default "";

    /**
     * Specify whether or not the {@link DataSet} resulting from
     * a call to the annotated method will be connected to the
     * database or not. This will generally only have an effect on
     * {@code DataSet}s.
     * 
     * @see DataSet
     * @see DataSet#disconnect
     * @return {@literal false} by default
     */
    boolean disconnected() default false;

    /**
     * If <code>rubberstamp</code> is flagged as <code>true</code>,
     * the annotated method must return a {@link DataIterator},
     * otherwise it will be rejected by the {@link QueryTool}.
     * Rubber-stamping will cause the returned {@code DataIterator}
     * to use a single instance of the data-object for all of the
     * rows returned, instead of initializing a new one for each row.
     * This can be used to save CPU time and memory, and is
     * especially useful in display code.
     *
     * @see DataIterator
     * @return {@literal false} by default
     */
    boolean rubberstamp() default false;

    /**
     * <p>
     * By deafult a {@link DataSet} returned by an {@code @Select} method is
     * read only. If you set this to {@literal false} the returned
     * {@code DataSet} will implement the following additional methods:
     * </p>
     * <ul>
     *  <li>{@link java.util.List#add(Object)}</li>
     *  <li>{@link java.util.List#addAll(java.util.Collection)}</li>
     *  <li>{@link java.util.List#remove(int)}</li>
     *  <li>{@link java.util.List#set(int, Object)}</li>
     * </ul>
     * <p>
     * It is considered a validation error for a method to have readOnly set
     * {@literal false} if it doesn't return a {@code DataSet}.
     * </p>
     *
     * @since 1.1
     * @return {@literal true} by default
     */
    boolean readOnly() default true;

    /**
     *<p>
     * The cache class to be used by {@code DataSet}s returned by the
     * annotated method. This only has an effect of methods that return a
     * {@link DataSet} type.
     *</p><p>
     * The default {@code DataSetCache} implementation is
     * {@link ArrayDataSetCache}, which uses an array to cache
     * the row values. For larger {@code ResultSet}s, you
     * may want to look into a {@link NullDataSetCache} or using
     * a {@link DataIterator} instead of the {@code DataSet} class.
     *</p>
     *
     * @see DataSet
     * @see ArrayDataSetCache
     * @return the {@code DataSetCache} type that the returned {@code DataSet}
     *      should proxy all of the returned objects through
     */
    Class<? extends DataSetCache> cache() default ArrayDataSetCache.class;

    /**
     * <p>
     * This is a hint to the database driver to suggest the number of rows
     * to fetch in a single page of the {@link ResultSet}. This value
     * directly corresponds to the {@link Statement#setFetchSize(int)}
     * method.
     * </p><p>
     * If this value is {@literal 0} (which it is by default) then the
     * fetch-size of the underlying {@code Statement} will be left alone.
     * This value (like the {@code fetch-size} of a {@code Statement}) is
     * to be considered a hint to EoD SQL. The underlying
     * {@link MethodImplementation} may choose to ignore this value.
     * </p>
     *
     * @since 2.1
     * @return the suggested number of rows to be fetched at a time
     */
    int fetchSize() default 0;

    /**
     * <p>
     * This attribute optionally allows you to populate one of the parameters
     * of the annotated method instead of returning a new object. The value
     * of this attribute must point to the index of the parameter to be
     * populated. The first parameter is {@literal 1}, the second is
     * {@literal 2} and so on. The parameter must be a valid data-object,
     * and thus may not be a {@code Collection}; array; primitive; etc.
     * </p><p>
     * This attribute can be used to either refresh data that has previously
     * been fetched from the database and/or to fetch new data into an object.
     * This allows for a lazy-loading or fetch-group style of data loading
     * from the database.
     * </p><p>
     * Finally: a method selecting into an existing object may only
     * return {@code void} <i>or</i> the same type as the specified
     * parameter (in which case the parameter itself will be returned).
     * </p>
     *
     * @return {@literal 0} by default to indicate that the method returns
     *      new objects instead of creating new ones
     * @since 2.1
     */
    int into() default 0;

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
    
    /**
     * <p>
     * A custom data object binding to be used on the result set of this query. The default value
     * {@link NoDataObjectBinding} indicates that no custom binding is to be used.
     * </p>
     * 
     * @since 2.2
     */
    Class<? extends DataObjectBinding> resultSetBinding() default NoDataObjectBinding.class;
}
