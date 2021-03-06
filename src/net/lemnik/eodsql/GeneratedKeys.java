package net.lemnik.eodsql;

import java.sql.Statement;

/**
 * Enumeration used by the {@link Update @Update} annotation to specify what
 * to do with keys generated by the database.
 *
 * @author Jason Morris
 *
 * @see Update
 * @see AutoGeneratedKeys
 */
public enum GeneratedKeys {

    /**
     * <p>
     * Specifies that a normal {@link java.sql.Statement#executeUpdate}
     * should be performed. In this case no keys will be requested from
     * the database, and any that are returned will be ignored.
     * </p><p>
     * This is the default value, and is mostly used with the SQL statement
     * is an {@code "UPDATE"} or when you are generating the primary key
     * in your application instead of in the database.
     * </p>
     */
    NO_KEYS_RETURNED,
    /**
     * <p>
     * Specifies that the JDBC driver should return whatever keys it
     * decides to. This is the equivalent of
     * {@code Statement.executeUpdate(String sql,
     * Statement.RETURN_GENERATED_KEYS)}
     * </p><p>
     * Databases that have the ability to auto-generate keys will generally
     * respond to this call with the columns they have constructed as part
     * of the {@code "INSERT"} query. Columns returned from the database
     * will be bound to the fields in the returned objects by their column
     * names (the same way as is done with non-key objects). Thus the names
     * of the columns returned by the database is significant.
     * </p><p>
     * Many JDBC drivers will return only simple primary keys, and will not
     * name the column as expected (if it is named at all). In this case
     * the {@link #RETURNED_KEYS_FIRST_COLUMN} value should be used, as it
     * works in a similar way, but ignores the column names returned.
     * </p>
     *
     * @see Statement#execute(String, int)
     * @see #RETURNED_KEYS_FIRST_COLUMN
     */
    RETURNED_KEYS_DRIVER_DEFINED,
    /**
     * <p>
     * This specifies that the generated keys should be explicitly requested
     * from the JDBC driver by name. This is the equivalent of
     * {@code Statement.executeUpdate(String sql, String [] colNames)}
     * </p><p>
     * Many JDBC drivers don't have this functionality and will throw an
     * {@code Exception} if this value is used for an update.
     * </p>
     *
     * @see Statement#execute(String, String[])
     */
    RETURNED_KEYS_COLUMNS_SPECIFIED,
    /**
     * <p>
     * Specifies that the the first column from of the generated keys should be
     * mapped as the AutoGeneratedKeys. Several JDBC drivers don't handle the
     * returning of more than one generated key, and often don't support
     * returning the key with it's real column name. This flag can be
     * used to work around such drivers.
     * </p><p>
     * This option can also be used as an optimization when a
     * non-composite auto-generated key is being used
     * (which is most of the time).
     * </p>
     *
     * @since 1.1
     * @see Statement#execute(String, int)
     */
    RETURNED_KEYS_FIRST_COLUMN

}
