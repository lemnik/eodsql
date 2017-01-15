package net.lemnik.eodsql;

import java.util.Collection;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;

/**
 * Test cases for the batch update feature.
 *
 * @author Bernd Rinn
 */
public class BatchUpdateTest extends EoDTestCase {

    private DataSetQuery query = null;

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

    private SimpleObject newSimpleObject(final int index) {
        final SimpleObject obj = new SimpleObject();
        obj.id = UUID.randomUUID();
        obj.data = Integer.toBinaryString(index);
        obj.order = index;

        return obj;
    }

    private void insertDataBatchArray(final Collection<SimpleObject> validation) {
        final SimpleObject[] objects = new SimpleObject[1000];

        for(int i = 0; i < objects.length; i++) {
            objects[i] = newSimpleObject(i);
            validation.add(objects[i]);
        }
        
        query.insertBatchArray(objects);
    }

    private void insertDataBatchCollection(final Collection<SimpleObject> validation) {
        for(int i = 0; i < 1000; i++) {
            final SimpleObject object = newSimpleObject(i);
            validation.add(object);
        }
        query.insertBatchCollection(validation);
    }

    private void insertDataBatchCollectionUniformData(
            final Collection<SimpleObject> validation, String data) {
        
        for(int i = 0; i < 1000; i++) {
            final SimpleObject object = newSimpleObject(i);
            validation.add(object);
        }

        query.insertBatchCollectionUniformData(validation, data);
    }

    private void insertDataBatchCollectionStartWithUniformData(int index,
            final Collection<SimpleObject> validation) {
        
        for(int i = 0; i < 1000; i++) {
            final SimpleObject object = newSimpleObject(i);
            validation.add(object);
        }

        query.insertBatchCollectionUniformData(index, validation);
    }

    public void testBatchUpdateArray() throws Exception {
        final Set<SimpleObject> validation = new HashSet<SimpleObject>();

        insertDataBatchArray(validation);

        final DataSet<SimpleObject> objects = query.getConnected();
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

    public void testBatchUpdateCollection() throws Exception {
        final Set<SimpleObject> validation = new HashSet<SimpleObject>();

        insertDataBatchCollection(validation);

        final DataSet<SimpleObject> objects = query.getConnected();
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

    public void testBatchUpdateCollectionUniformData() throws Exception {
        final Set<SimpleObject> validation = new HashSet<SimpleObject>();

        final String data = "some data";
        insertDataBatchCollectionUniformData(validation, data);

        final DataSet<SimpleObject> objects = query.getConnected();
        final ListIterator<SimpleObject> li = objects.listIterator();

        assertFalse(li.hasPrevious());
        assertTrue(li.hasNext());
        assertEquals(0, li.nextIndex());
        assertEquals(-1, li.previousIndex());

        final SimpleObject so = li.next();

        assertNotNull(so);
        assertEquals(data, so.data);

        assertTrue(li.hasPrevious());
        assertTrue(li.hasNext());
        assertEquals(0, li.previousIndex());
        assertEquals(1, li.nextIndex());

        objects.close();
    }

    public void testBatchUpdateCollectionStartWithUniformData() throws Exception {
        final Set<SimpleObject> validation = new HashSet<SimpleObject>();

        insertDataBatchCollectionStartWithUniformData(99, validation);

        final DataSet<SimpleObject> objects = query.getConnected();
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

}
