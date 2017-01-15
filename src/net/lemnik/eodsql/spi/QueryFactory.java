package net.lemnik.eodsql.spi;

import java.sql.Connection;

import javax.sql.DataSource;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.InvalidQueryException;

/**
 * <p>
 * Implementing a <code>QueryFactory</code> allows for alternative implementations
 * of query objects. When the {@link net.lemnik.eodsql.QueryTool#getQuery(java.lang.Class) getQuery}
 * methods are invoked, the following process is followed:
 * </p>
 * 
 * <ol>
 *  <li>
 *      The query class is validated using the default validation rules. A failed validation will
 *      result in an {@link net.lemnik.eodsql.InvalidQueryException} being thrown.
 *  </li>
 *  <li>
 *      Any registered <code>QueryFactory</code> objects have their {@link #canConstruct(java.lang.Class)}
 *      methods invoked. The first to return <code>true</code> will be used to construct the implementation
 *      using it's
 *      {@link #construct(java.sql.Connection, java.lang.Class query, java.lang.ClassLoader loader) construct}
 *      method.
 *  </li>
 *  <li>
 *      If no <code>QueryFactory</code> returned <code>true</code> from it's <code>canConstruct</code> method,
 *      the default query construction will take place.
 *  </li>
 * </ol>
 * 
 * <p>
 *  <code>QueryFactory</code> is classed as a {@link java.util.ServiceLoader Service} class. If
 *  the Java VM is not at least 1.6, custom <code>QueryFactory</code> classes will be ignored.
 * </p>
 *
 * @author Jason Morris
 * @since 1.1
 */
public interface QueryFactory {

    /**
     * Used by the {@link net.lemnik.eodsql.QueryTool} to discover if this
     * <code>QueryFactory</code> is able to create an implementation of
     * the specified {@link net.lemnik.eodsql.BaseQuery} interface.
     * 
     * @param query the interface type
     * @return <code>true</code> if this <code>QueryFactory</code> can
     *  create an implementation of the specified interface.
     */
    boolean canConstruct(Class<? extends BaseQuery> query);

    /**
     * Create an implementation of the specified query interface. The <code>Connection</code> given
     * should be bound to the implementation, and closed when the object is closed. If a
     * <code>ClassLoader</code> is required to create the implementation, the specified
     * <code>ClassLoader</code> should be used.
     * 
     * @param connection the database <code>Connection</code> that the implementation should wrap
     * @param query the query interface to implement
     * @param loader the <code>ClassLoader</code> to use, if one is needed
     * @return the query implementation
     * @throws InvalidQueryException if <code>query</code> doesn't follow additional validation checks imposed by this
     *  <code>QueryFactory</code>
     */
    <T extends BaseQuery> T construct(
            Connection connection,
            Class<T> query,
            ClassLoader loader)
            throws InvalidQueryException;

    /**
     * Create an implementation of the specified query interface. The specified <code>DataSource</code>
     * should be used by the implementation as a source of <code>Connection</code> objects. It is
     * assumed that the specified <code>DataSource</code> is pooled. If a <code>ClassLoader</code> is
     * required to create the implementation, the specified <code>ClassLoader</code> should be used.
     * 
     * @param dataSource used by the implementation to fetch <code>Connection</code> objects
     * @param query the query interface to implement
     * @param loader the <code>ClassLoader</code> to use, if one is needed
     * @return the query implementation
     * @throws InvalidQueryException if <code>query</code> doesn't follow additional validation checks imposed by this
     *  <code>QueryFactory</code>
     */
    <T extends BaseQuery> T construct(
            DataSource dataSource,
            Class<T> query,
            ClassLoader loader)
            throws InvalidQueryException;

}
