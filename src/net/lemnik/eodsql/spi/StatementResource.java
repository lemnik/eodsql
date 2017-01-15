package net.lemnik.eodsql.spi;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * A simple {@link Resource} implementation wrapping a {@code Statement}
 * object for use within a {@link MethodImplementation}.
 *
 * @author Jason Morris
 */
public class StatementResource implements Resource<Statement> {

    private Statement statement;

    private boolean closed = false;

    /**
     * Create a new {@code StatementResource} wrapping a specified
     * {@code Statement}.
     *
     * @param statement the {@code Statement} that the new
     *      {@code StatementResource} needs to wrap
     * @throws IllegalArgumentException if the given {@code Statement} is
     *      {@literal null}
     */
    public StatementResource(
            final Statement statement)
            throws IllegalArgumentException {

        if(statement == null) {
            throw new IllegalArgumentException("Statement cannot be null.");
        }

        this.statement = statement;
    }

    public Statement get() {
        return statement;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() throws SQLException {
        if(!closed) {
            statement.close();
            closed = true;
        }
    }

    public Class<Statement> getResourceType() {
        return Statement.class;
    }

}
