package net.lemnik.eodsql;

import java.sql.Savepoint;

/**
 * <p>
 *    An extension to the default <code>BaseQuery</code> that holds a transaction.
 *    A <code>TransactionQuery</code> opens a database transaction when it is
 *    created, and as a side-effect, also holds a <code>Connection</code> to the
 *    database. Unlike a normal <code>BaseQuery</code>, a
 *    <code>TransactionQuery</code> will not affect the database until the
 *    {@link #commit()} method is invoked.
 * </p>
 * @author jason
 */
public interface TransactionQuery extends BaseQuery {
    /**
     * Closes this <code>TransactionQuery</code> optionally calling
     * {@link #commit()} before closing the <code>Connection</code> to the
     * database.
     *
     *@param commit <code>true</code> if {@link #commit()} should be called
     *  before closing, or <code>false</code> if nothing is done to the
     *  underlying <code>Connection</code> before closing.
     */
    void close(boolean commit);

    /**
     * Makes all the changes to the database since the last commit / rollback
     * permanent.
     */
    void commit();

    /**
     * Undoes any changes made by this <code>TransactionQuery</code>.
     */
    void rollback();

    /**
     * Undoes any changes from the specified <code>Savepoint</code> up to the
     * current location in the transaction.
     * @param savepoint the <code>Savepoint</code> that marks the current position in the transaction
     */
    void rollback(Savepoint savepoint);

    /**
     * Sets an unnamed save-point at the current location in the transaction.
     * This <code>Savepoint</code> can be used to
     * {@link #rollback(Savepoint) rollback} the transaction to this point.
     *
     *@return a new, unnamed <code>Savepoint</code> for this location in the
     *  transaction
     */
    Savepoint setSavepoint();

    /**
     * Sets a named <code>Savepoint</code> at the current position within the
     * transaction.
     * @param name the name of the new <code>Savepoint</code>
     * @return the <code>Savepoint</code> at the current location in the transaction
     */
    Savepoint setSavepoint(String name);

}
