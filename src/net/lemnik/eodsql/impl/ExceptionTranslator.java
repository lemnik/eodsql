package net.lemnik.eodsql.impl;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * A roles that translates {@link SQLException}s into RuntimeExceptions.
 * 
 * @author Bernd Rinn
 */
public interface ExceptionTranslator {

    /**
     * Translates the exception <var>ex</var>.
     * 
     * @param dataSource
     *            the DataSource that the exception occurred on.
     * @param task
     *            the task when the exception occurred.
     * @param sql
     *            the sql command when the exception occurred.
     * @param ex
     *            the original exception.
     */
    public RuntimeException translateException(DataSource dataSource,
	    String task, String sql, SQLException ex);

    /**
     * Translates the exception <var>ex</var>.
     * 
     * @param connection
     *            the Connection that the exception occurred on. Can be expected to be open.
     * @param task
     *            the task when the exception occurred.
     * @param sql
     *            the sql command when the exception occurred.
     * @param ex
     *            the original exception.
     */
    public RuntimeException translateException(Connection connection,
	    String task, String sql, SQLException ex);

    /**
     * Returns an exception when a unique result is expected from the database,
     * but the database returns multiple rows.
     */
    public RuntimeException uniqueResultExpected();
}
