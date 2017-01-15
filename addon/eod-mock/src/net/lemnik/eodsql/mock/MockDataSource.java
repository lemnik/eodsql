package net.lemnik.eodsql.mock;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * <p>
 * </p><p>
 * Created on 02 Feb 2010
 * </p>
 *
 * @author Jason Morris
 */
class MockDataSource implements DataSource {

    public Connection getConnection() throws SQLException {
        throw new UnsupportedOperationException("Mock DataSource in use");
    }

    public Connection getConnection(
            final String username,
            final String password)
            throws SQLException {
        
        throw new UnsupportedOperationException("Mock DataSource in use");
    }

    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException("Mock DataSource in use");
    }

    public void setLogWriter(
            final PrintWriter out) throws SQLException {

        throw new UnsupportedOperationException("Mock DataSource in use");
    }

    public void setLoginTimeout(final int seconds) throws SQLException {
        throw new UnsupportedOperationException("Mock DataSource in use");
    }

    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Mock DataSource in use");
    }

    public <T> T unwrap(final Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException("Mock DataSource in use");
    }

    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException("Mock DataSource in use");
    }

}
