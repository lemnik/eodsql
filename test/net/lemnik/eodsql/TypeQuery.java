/*
 * TypeQuery.java
 *
 * Created on March 13, 2007, 3:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.lemnik.eodsql;

import java.sql.SQLException;

/**
 *
 * @author jason
 */
public interface TypeQuery extends BaseQuery {
    @Select("SELECT * FROM types_table")
    public DataSet<TypeObject> getTypes();
    
    @Update("INSERT INTO types_table (byte_col, short_col, int_col, " +
            "long_col, float_col, double_col, bool_col, string_col, " +
            "date_col, uuid_col) VALUES (?{1.byteObject}, ?{1.shortObject}, " +
            "?{1.intObject}, ?{1.longObject}, ?{1.floatObject}, " +
            "?{1.doubleObject}, ?{1.booleanObject}, ?{1.stringObject}, " +
            "?{1.dateObject}, ?{1.uuidObject})")
    public void insert(TypeObject obj);
    
    @Select("SELECT * FROM types_table")
    public TypeObject getFirstObject();
    
    @Update("DELETE FROM types_table")
    public void clearTable();
    
    @Update("CREATE TABLE types_table (byte_col SMALLINT, short_col SMALLINT, " +
            "int_col INTEGER, long_col BIGINT, float_col FLOAT, double_col DOUBLE, " +
            "bool_col BOOLEAN, string_col VARCHAR(100), date_col TIMESTAMP, " +
            "uuid_col CHAR(36))")
    public void createTypesTable();
    @Update ("drop table types_table")
    public void dropTable() throws SQLException;
}
