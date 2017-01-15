package net.lemnik.eodsql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author jason
 */
public abstract class AbstractDataSetTestObject extends EoDTestCase {

    protected DataSetQuery query = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        query = QueryTool.getQuery(
                getConnection(),
                DataSetQuery.class);

        query.createObjectsTable();
    }

    @Override
    protected void tearDown() throws Exception {
        query.dropTable();
        query.close();
        super.tearDown();
    }

    protected SimpleObject newSimpleObject(final int index) {
        final SimpleObject obj = new SimpleObject();
        obj.id = UUID.randomUUID();
        obj.data = Integer.toBinaryString(index);
        obj.order = index;

        return obj;
    }

    protected void insertData(final Collection<SimpleObject> validation) {
        for(int i = 0; i < 1000; i++) {
            final SimpleObject obj = newSimpleObject(i);

            query.insert(obj);
            validation.add(obj);
        }
    }

    protected void assertDataSetEquals(
            final List<SimpleObject> validation,
            final DataSet<SimpleObject> objects) {

        assertEquals(
                "DataSet size is not equal to the number of objects inserted",
                validation.size(),
                objects.size());

        for(int i = 0; i < validation.size(); i++) {
            assertEquals(validation.get(i), objects.get(i));
        }
    }

    protected abstract DataSet<SimpleObject> getDataSet() throws Exception;

    public void testListIteratorDefault() throws Exception {
        final Set<SimpleObject> validation = new HashSet<SimpleObject>();

        insertData(validation);

        final DataSet<SimpleObject> objects = getDataSet();
        final ListIterator<SimpleObject> li = objects.listIterator();

        assertFalse(li.hasPrevious());
        assertTrue(li.hasNext());
        assertEquals(0, li.nextIndex());
        assertEquals(-1, li.previousIndex());

        final SimpleObject so = li.next();

        assertNotNull(so);

        assertTrue(li.hasPrevious());
        assertTrue(li.hasNext());
        assertEquals(0, li.previousIndex());
        assertEquals(1, li.nextIndex());

        objects.close();
    }

    public void testSubList() throws Exception {
        final List<SimpleObject> validation = new ArrayList<SimpleObject>();

        insertData(validation);

        final DataSet<SimpleObject> objects = getDataSet();

        final List<SimpleObject> sublist = objects.subList(100, 200);
        final List<SimpleObject> slvalidation = validation.subList(100, 200);

        assertEquals(
                "Sublist size is wrong.",
                slvalidation.size(),
                sublist.size());

        for(int i = 0; i < 100; i++) {
            assertEquals(slvalidation.get(i), sublist.get(i));
            assertEquals(objects.get(i + 100), sublist.get(i));
        }

        assertEquals(
                "List equality failed.",
                slvalidation,
                sublist);
    }

    public void testDataContents() throws Exception {
        final List<SimpleObject> validation = new ArrayList<SimpleObject>();

        insertData(validation);

        final DataSet<SimpleObject> objects = getDataSet();
        assertDataSetEquals(validation, objects);

        objects.close();
    }
    
}
