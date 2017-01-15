package net.lemnik.eodsql.spi;

import java.sql.SQLException;

/**
 *
 * @author Jason Morris
 */
public interface Resource<T> {

    T get();

    boolean isClosed();

    void close() throws SQLException;

    Class<T> getResourceType();

}
