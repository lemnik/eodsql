package net.lemnik.eodsql;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import net.lemnik.eodsql.AccessibilityTestQuery.InaccessibleObject;

/**
 * Created on 2008/08/01
 * @author Jason Morris
 */
public class AccessibilityTest extends EoDTestCase {
    AccessibilityTestQuery query = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        query = QueryTool.getQuery(getConnection(), AccessibilityTestQuery.class);
        query.create();
    }

    @Override
    protected void tearDown() throws Exception {
        query.drop();
        query.close();
        
        super.tearDown();
    }
    
    public void testAccessibility() throws Exception {
        List<InaccessibleObject> objects = new ArrayList<InaccessibleObject>(100);
        
        Random random = new Random();
        for(int i = 0; i < 100; i++) {
            InaccessibleObject object = new InaccessibleObject(random.nextLong(), UUID.randomUUID().toString());
            query.insert(object);
            objects.add(object);
        }
        
        InaccessibleObject[] found = query.select();
        
        assertEquals(objects.size(), found.length);
        
        for(int i = 0; i < found.length; i++) {
            assertTrue("Couldn't find object: " + found[i] + " index " + i, objects.contains(found[i]));
        }
    }

}
