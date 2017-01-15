/*
 * PrimitiveQuery.java
 *
 * Created on April 6, 2007, 11:05 AM
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
public interface PrimitiveQuery extends BaseQuery {

    @Update(
    "INSERT INTO primitive_table (my_integer, my_double) VALUES (?1, ?2)")
    public void insert(int intValue, double doubleValue);

    @Select("SELECT MAX(my_integer) FROM primitive_table")
    public int maxInt();

    @Select("SELECT MIN(my_integer) FROM primitive_table")
    public int minInt();

    @Select("SELECT AVG(my_double) FROM primitive_table")
    public double averageDouble();

    @Select("SELECT my_integer FROM primitive_table ORDER BY my_integer ASC")
    public int[] sortedInts();

    @Update("DELETE FROM primitive_table")
    public void deleteAll();

    @Update("CREATE TABLE primitive_table (my_integer INTEGER, my_double DOUBLE)")
    public void createPrimitiveTable();

    @Update("DROP TABLE primitive_table")
    public void dropPrimitiveTable() throws SQLException;

}
