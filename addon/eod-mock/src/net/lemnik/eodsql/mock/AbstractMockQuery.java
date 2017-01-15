package net.lemnik.eodsql.mock;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.EoDException;

/**
 * <p>
 * </p><p>
 * Created on 02 Feb 2010
 * </p>
 *
 * @author Jason Morris
 */
public abstract class AbstractMockQuery implements BaseQuery {

    private boolean closed = false;

    protected void checkClosed() {
        if(closed) {
            throw new EoDException("Query has been closed: " +
                    getClass().getName());
        }
    }

    public void close() {
        closed = true;
    }

    public boolean isClosed() {
        return closed;
    }

}
