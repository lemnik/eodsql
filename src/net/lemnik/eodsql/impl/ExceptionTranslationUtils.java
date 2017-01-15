package net.lemnik.eodsql.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.impl.BaseQueryImpl.Callable;
import net.lemnik.eodsql.impl.BaseQueryImpl.ConnectionSource;
import net.lemnik.eodsql.impl.BaseQueryImpl.DataSourceConnectionSource;
import net.lemnik.eodsql.impl.BaseQueryImpl.MethodImpl;

/**
 * A class that supports exception translation of {@link SQLException} into a
 * custom exception hierarchy.
 * 
 * @author Bernd Rinn
 */
public class ExceptionTranslationUtils {

    private static ExceptionTranslator exceptionTranslator = new DefaultExceptionTranslator();

    private static class DefaultExceptionTranslator implements
            ExceptionTranslator {

        public RuntimeException translateException(DataSource dataSource,
                String task, String sql, SQLException ex) {
            return new EoDException("'" + task + "' [SQL: '" + sql + "']",
                    ex);
        }

        public RuntimeException translateException(Connection connection,
                String task, String sql, SQLException ex) {
            return new EoDException("'" + task + "' [SQL: '" + sql + "']",
                    ex);
        }

        public RuntimeException uniqueResultExpected() {
            return new EoDException(
                    "A unique result was expected but the database returned multiple rows.");
        }
    }

    /**
     * Sets the {@link ExceptionTranslator} for EoDSQL. Call once before EoDSQL
     * is used.
     */
    public static void setExceptionTranslator(ExceptionTranslator factory) {
        exceptionTranslator = factory;
    }

    /**
     * Translates the exception <var>ex</var> to a RuntimeException.
     */
    public static RuntimeException translateException(
            ConnectionSource connectionSource, final Method method,
            final Callable callable, final Exception ex) {
        return translateException(connectionSource, method.getName(),
                getSql(callable), ex);
    }

    /**
     * Translates the exception <var>ex</var> to a RuntimeException.
     */
    public static RuntimeException translateException(
            ConnectionSource connectionSource, final String task,
            final String sql, final Exception ex) {
        if(connectionSource instanceof DataSourceConnectionSource) {
            final DataSource source = ((DataSourceConnectionSource)connectionSource).getDataSource();
            return translateException(source, task, sql, ex);
        } else {
            Connection conn = null;
            try {
                conn = connectionSource.getConnection();
                return translateException(conn, task, sql, ex);
            } catch(SQLException ex1) {
                return new EoDException(toMessage(task, sql), ex);
            } finally {
                if(conn != null) {
                    try {
                        connectionSource.releaseConnection(conn);
                    } catch(SQLException ex1) {
                        // Nothing we can do here.
                    }
                }
            }
        }
    }

    /**
     * Translates the exception <var>ex</var> to a RuntimeException.
     */
    public static RuntimeException translateException(DataSource dataSource,
            final Method method, final Callable callable, final Exception ex) {
        return translateException(dataSource, method.getName(),
                getSql(callable), ex);
    }

    /**
     * Translates the exception <var>ex</var> to a RuntimeException.
     */
    public static RuntimeException translateException(DataSource dataSource,
            final String task, final String sql, final Exception ex) {
        if(ex instanceof SQLException) {
            return exceptionTranslator.translateException(dataSource, task,
                    sql, (SQLException)ex);
        } else {
            return new EoDException(toMessage(task, sql), ex);
        }
    }

    /**
     * Translates the exception <var>ex</var> to a RuntimeException.
     */
    public static RuntimeException translateException(Connection connection,
            final Method method, final Callable callable, final Exception ex) {
        return translateException(connection, method.getName(),
                getSql(callable), ex);
    }

    /**
     * Translates the exception <var>ex</var> to a RuntimeException.
     */
    public static RuntimeException translateException(Connection connection,
            final String task, final String sql, final Exception ex) {
        if(ex instanceof SQLException) {
            return exceptionTranslator.translateException(connection, task,
                    sql, (SQLException)ex);
        } else {
            return new EoDException(toMessage(task, sql), ex);
        }
    }

    /**
     * Returns an exception when a unique result is expected from the database,
     * but the database returns multiple rows.
     */
    public static RuntimeException uniqueResultExpected() {
        return exceptionTranslator.uniqueResultExpected();
    }

    private static String toMessage(final String task, final String sql) {
        return task + "[SQL: '" + sql + "'].";
    }

    private static String getSql(final Callable callable) {
        if(callable instanceof MethodImpl) {
            final AbstractMethodImplementation<Annotation> m = ((MethodImpl)callable).getMethodImpl();
            return m.query.toString();
        } else {
            return "?";
        }
    }
}
