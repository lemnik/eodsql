package net.lemnik.eodsql.spi.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.lemnik.eodsql.EoDException;

/**
 * A tag class indicating that no custom data object binding is to be used.
 *  
 * @author Bernd Rinn
 */
public final class NoDataObjectBinding<T> extends DataObjectBinding<T> {
    @Override
    public void marshall(T from, ResultSet results) throws SQLException, EoDException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unmarshall(ResultSet row, T into) throws SQLException, EoDException {
        throw new UnsupportedOperationException();
    }
}
