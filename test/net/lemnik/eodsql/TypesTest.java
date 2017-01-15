/*
 * TypesTest.java
 * JUnit based test
 *
 * Created on March 13, 2007, 3:56 PM
 */

package net.lemnik.eodsql;

import java.util.Date;
import java.util.UUID;

/**
 *
 * @author jason
 */
public class TypesTest extends EoDTestCase {
	TypeQuery query=null;
	
    public TypesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        query = QueryTool.getQuery(getConnection(), TypeQuery.class);
        query.createTypesTable();
    }
    
    public void testSingleObject() throws Exception {
        Date today = new Date();
        UUID myUUID = UUID.randomUUID();
        TypeQuery query = QueryTool.getQuery(getConnection(), TypeQuery.class);
        assertNotNull("Query was not created!", query);
        
        TypeObject tmp = new TypeObject();
        tmp.byteObject = new Byte((byte)10);
        tmp.shortObject = new Short((short)15);
        tmp.intObject = new Integer(20);
        tmp.longObject = new Long(25l);
        tmp.floatObject = new Float(1.5f);
        tmp.doubleObject = new Double(2.5f);
        tmp.booleanObject = Boolean.TRUE;
        tmp.dateObject = today;
        tmp.stringObject = "Hello World";
        tmp.uuidObject = myUUID;
        
        query.insert(tmp);
        
        DataSet<TypeObject> data = query.getTypes();
        assertNotNull("Returned DataSet is null!", data);
        
        TypeObject tmp2 = data.get(0);
        assertNotNull("First returned item is null!", tmp2);
        
        assertEquals("byte", tmp.byteObject, tmp2.byteObject);
        assertEquals("short", tmp.shortObject, tmp2.shortObject);
        assertEquals("int", tmp.intObject, tmp2.intObject);
        assertEquals("long", tmp.longObject, tmp2.longObject);
        assertEquals("float", tmp.floatObject, tmp2.floatObject);
        assertEquals("double", tmp.doubleObject, tmp2.doubleObject);
        assertEquals("boolean", tmp.booleanObject, tmp2.booleanObject);
        assertFalse("date", tmp.dateObject.before( tmp2.dateObject)&&tmp.dateObject.after( tmp2.dateObject));
        assertEquals("string", tmp.stringObject, tmp2.stringObject);
        assertEquals("uuid", tmp.uuidObject, tmp2.uuidObject);
        
        data.close();
    }

	@Override
	protected void tearDown() throws Exception {
		try {
			query = QueryTool.getQuery(getConnection(), TypeQuery.class);
			query.dropTable();
		} finally {
			super.tearDown();
		}
	}
    
}
