/*
 * TransactionTestQuery.java
 *
 * Created on March 15, 2007, 1:11 PM
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
public interface TransactionTestQuery extends TransactionQuery {
    @Update("INSERT INTO objects_table (id, data) VALUES(?{1.id}, ?{1.data})")
    public void insert(SimpleObject object);

    @Select("SELECT * FROM objects_table")
    public DataSet<SimpleObject> getObjects();

    @Update("DELETE FROM objects_table")
    public void clearTable();

    @Update("CREATE TABLE objects_table (id CHAR(36) NOT NULL PRIMARY KEY, data VARCHAR(100));")
    public void createObjectsTable();

    @Update("drop table objects_table")
    public void dropObjectsTable() throws SQLException;

}
