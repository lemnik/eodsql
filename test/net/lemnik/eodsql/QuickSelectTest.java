/*
 * Copyright Jason Morris 2008. All rights reserved.
 */
package net.lemnik.eodsql;

import java.sql.Connection;
import java.util.UUID;

/**
 * Created on 2008/07/31
 * @author Jason Morris
 */
public class QuickSelectTest extends EoDTestCase {
    private DataSetQuery query;
    private Connection connection;

    public QuickSelectTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        query = QueryTool.getQuery(connection = getConnection(), DataSetQuery.class);
        query.createObjectsTable();
    }

    @Override
    protected void tearDown() throws Exception {
        query.dropTable();
        query.close();
        
        super.tearDown();
    }
    
    public void testQuickSelect() throws Exception {
        SimpleObject object = new SimpleObject();
        object.data = "My Data";
        object.id = UUID.randomUUID();
        
        query.insert(object);
        
        DataSet<SimpleObject> results = QueryTool.select(connection, SimpleObject.class, "SELECT * FROM objects WHERE data = ?1", object.data);
        
        assertEquals(1, results.size());
        assertEquals(object, results.get(0));
        
        results.close();
    }

}
