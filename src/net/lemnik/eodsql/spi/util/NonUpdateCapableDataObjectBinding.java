package net.lemnik.eodsql.spi.util;

import java.sql.ResultSet;

/**
 * A base class for {@link DataObjectBinding} implementations that are not capable of updates.
 *
 * @author Bernd Rinn
 */
public abstract class NonUpdateCapableDataObjectBinding<T> extends DataObjectBinding<T>
{
    @Override
    public boolean isUpdateCapable()
    {
        return false;
    }

    @Override
    public void marshall(T from, ResultSet results) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
