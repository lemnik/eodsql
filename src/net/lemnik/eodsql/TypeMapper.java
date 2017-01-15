package net.lemnik.eodsql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/**
 *<p>
 * A <code>TypeMapper</code> allows for interaction with the SQL -> Java
 * mapping process. Types in EoD SQL are mapped based on what the Java
 * type is, not what the SQL type is, this allows for greater flexibility
 * since many Java types can all map to the same SQL type.
 *</p><p>
 * By default most data-types you will need already have <code>TypeMapper</code>s:
 * <ul>
 *  <li>String</li>
 *  <li>Byte / byte</li>
 *  <li>Short / short</li>
 *  <li>Integer / int</li>
 *  <li>Long / long</li>
 *  <li>Float / float</li>
 *  <li>Double / double</li>
 *  <li>Boolean / boolean</li>
 *  <li>Data</li>
 *  <li>UUID - Mapped to any String SQL type</li>
 *  <li>
 * </ul>
 *</p>
 *
 * @author jason
 */
public interface TypeMapper<T> {

    /**
     * Attempts to convert the result of the specified column in the
     * <code>ResultSet</code> into the type this <code>TypeMapper</code>
     * returns.
     *
     *@param results the <code>ResultSet</code> to fetch data from
     *@param column the column number in the <code>ResultSet</code>
     *@return the Java version of the SQL type
     *@throws SQLException if the SQL type cannot be mapped with
     * this <code>TypeMapper</code>
     */
    T get(ResultSet results, int column) throws SQLException;

    /**
     * Attempts to convert the spcified object into a SQL object, and
     * place it into a <code>ResultSet</code>. This method is used by
     * {@link net.lemnik.eodsql.Select#readOnly updatable} {@link net.lemnik.eodsql.DataSet DataSets}.
     * 
     * @param results the <code>ResultSet</code> to place the value into
     * @param column the column in the <code>ResultSet</code> where the data should be put
     * @param obj the object to map
     * @throws java.sql.SQLException if the object cannot be mapped with this
     *  <code>TypeMapper</code>
     */
    void set(ResultSet results, int column, T obj) throws SQLException;

    /**
     * Attempts to convert the specified object into a SQL object and
     * place it into the <code>PreparedStatement</code>.
     *
     *@param statement the <code>PreparedStatement</code> to place the data into
     *@param column the column in the statement where the data should be put
     *@param obj the object to map
     *@throws SQLException if the object cannot be mapped with this
     *  <code>TypeMapper</code>
     */
    void set(PreparedStatement statement, int column, T obj) throws SQLException;

}
