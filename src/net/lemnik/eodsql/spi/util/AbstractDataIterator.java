package net.lemnik.eodsql.spi.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Iterator;
import java.util.NoSuchElementException;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.EoDException;

import net.lemnik.eodsql.spi.Context;

/**
 * Created on 2008/07/23
 * @author Jason Morris
 */
abstract class AbstractDataIterator<T> implements DataIterator<T> {
    private Context<?> context;

    protected ResultSet results;

    protected DataObjectBinding<T> binding;

    private T nextObject = null;

    private boolean closed = false;

    protected AbstractDataIterator(
            final Context<?> context,
            final DataObjectBinding<T> binding) {

        this.context = context;
        this.binding = binding;

        this.results = context.getResource(ResultSet.class).get();
    }

    private void ensureOpen() {
        if(closed) {
            throw new EoDException("A DataIterator's methods may not " +
                    "be invoked if it is closed.");
        }
    }

    protected abstract T unmarshal() throws SQLException;

    public void close() {
        if(!closed) {
            try {
                context.close();
                closed = true;
            } catch(SQLException sqle) {
                throw new EoDException(sqle);
            }
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean hasNext() {
        ensureOpen();

        if(nextObject != null) {
            return true;
        } else {
            try {
                if(results.next()) {
                    nextObject = unmarshal();
                    return true;
                } else {
                    close();
                    return false;
                }
            } catch(SQLException sqle) {
                throw new EoDException(sqle);
            }
        }
    }

    public T next() {
        if(hasNext()) {
            T returnObject = nextObject;
            nextObject = null;

            return returnObject;
        } else {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported " +
                "in a DataIterator.");
    }

    public Iterator<T> iterator() {
        return this;
    }

}
