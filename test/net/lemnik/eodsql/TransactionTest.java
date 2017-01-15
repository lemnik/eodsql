/*
 * TransactionTest.java
 * JUnit based test
 *
 * Created on March 15, 2007, 1:19 PM
 */
package net.lemnik.eodsql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import java.util.UUID;

/**
 *
 * @author jason
 */
public class TransactionTest extends EoDTestCase {
    private static boolean tableCreated = false;

    TransactionTestQuery query = null;

    public TransactionTest(String testName) {
        super(testName);
    }

    protected Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        Connection c = super.getConnection();
        c.setAutoCommit(false);
        return c;
    }

    protected void setUp() throws Exception {
        query = QueryTool.getQuery(getConnection(), TransactionTestQuery.class);

        //  if(!tableCreated) {
        query.createObjectsTable();
        query.commit();
    //     tableCreated = true;
    // }
    }

    public void testCommit() throws Exception {
        TransactionTestQuery query = QueryTool.getQuery(getConnection(), TransactionTestQuery.class);

        // generate 100 objects into the database
        for(int i = 0; i < 100; i++) {
            SimpleObject obj = new SimpleObject();
            obj.id = UUID.randomUUID();
            obj.data = Integer.toBinaryString(i);

            query.insert(obj);
        }

        query.commit();

        DataSet<SimpleObject> objects = query.getObjects();
        assertEquals("data set is the wrong length.", 100, objects.size());
        objects.close();
    }
    
    public void testTwoCommits() throws Exception {
        TransactionTestQuery query = QueryTool.getQuery(getDataSource(false), TransactionTestQuery.class);

        // generate 100 objects into the database
        for(int i = 0; i < 100; i++) {
            SimpleObject obj = new SimpleObject();
            obj.id = UUID.randomUUID();
            obj.data = Integer.toBinaryString(i);

            query.insert(obj);
        }

        query.commit();
        query.close();

        DataSet<SimpleObject> objects = query.getObjects();
        assertEquals("data set is the wrong length.", 100, objects.size());
        objects.close();

        // generate another 100 objects into the database
        for(int i = 0; i < 100; i++) {
            SimpleObject obj = new SimpleObject();
            obj.id = UUID.randomUUID();
            obj.data = Integer.toBinaryString(100 + i);

            query.insert(obj);
        }

        query.commit();
        DataSet<SimpleObject> objects2 = query.getObjects();
        assertEquals("data set is the wrong length.", 200, objects2.size());
        objects2.close();
    }
    
    // This test fails in Derby!!!
    public void testRollback() throws Exception {
        TransactionTestQuery query = QueryTool.getQuery(getConnection(), TransactionTestQuery.class);
        for(int i = 0; i < 100; i++) {
            SimpleObject obj = new SimpleObject();
            obj.id = UUID.randomUUID();
            obj.data = Integer.toBinaryString(i);

            query.insert(obj);
        }

        query.commit();
        query.clearTable();

        DataSet<SimpleObject> objects = query.getObjects();
        assertTrue("data set should be empty", objects.isEmpty());
        objects.close();

        query.rollback();

        objects = query.getObjects();
        assertEquals("data set is the wrong length.", 100, objects.size());
        objects.close();
    }

    public void testOneRollbackOneCommit() throws Exception {
        TransactionTestQuery query = QueryTool.getQuery(getDataSource(false), TransactionTestQuery.class);

        // generate 100 objects into the database
        for(int i = 0; i < 100; i++) {
            SimpleObject obj = new SimpleObject();
            obj.id = UUID.randomUUID();
            obj.data = Integer.toBinaryString(i);

            query.insert(obj);
        }

        query.rollback();
        query.close();

        DataSet<SimpleObject> objects = query.getObjects();
        assertEquals("data set is the wrong length.", 0, objects.size());
        objects.close();

        // generate another 100 objects into the database
        for(int i = 0; i < 100; i++) {
            SimpleObject obj = new SimpleObject();
            obj.id = UUID.randomUUID();
            obj.data = Integer.toBinaryString(100 + i);

            query.insert(obj);
        }

        query.commit();
        DataSet<SimpleObject> objects2 = query.getObjects();
        assertEquals("data set is the wrong length.", 100, objects2.size());
        objects2.close();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        query = QueryTool.getQuery(getConnection(), TransactionTestQuery.class);
        query.dropObjectsTable();
    }

}
