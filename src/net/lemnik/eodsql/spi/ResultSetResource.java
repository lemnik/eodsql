package net.lemnik.eodsql.spi;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * An implementation of a {@link Resource} that wraps a {@code ResultSet}
 * object.
 *
 * @author Jason Morris
 */
public class ResultSetResource implements Resource<ResultSet> {
    private final ResultSet results;

    private boolean closed = false;

    /**
     * Create a new {@code ResultSetResource} wrapping a specific
     * {@code ResultSet} object.
     *
     * @param results the {@code ResultSet} the new {@code ResultSetResource}
     *      should wrap
     * @throws IllegalArgumentException if the specified {@code ResultSet}
     *      is {@literal null}
     */
    public ResultSetResource(final ResultSet results)
            throws IllegalArgumentException {
        
        if(results == null) {
            throw new IllegalArgumentException("ResultSet cannot be null.");
        }

        this.results = results;
    }

    public ResultSet get() {
        return results;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() throws SQLException {
        if(!closed) {
            results.close();
            closed = true;
        }
    }

    public Class<ResultSet> getResourceType() {
        return ResultSet.class;
    }
}
