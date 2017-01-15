package net.lemnik.eodsql;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;

/**
 * Created on Sep 16, 2009
 * @author Jason Morris
 */
public class CollectionTest extends EoDTestCase {

    private CollectionQuery query;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        query = QueryTool.getQuery(getConnection(), CollectionQuery.class);
        query.create();
    }

    @Override
    protected void tearDown() throws Exception {
        query.drop();
        query = null;

        super.tearDown();
    }

    private Date date(int year, int month, int day) {
        return (new GregorianCalendar(year, month, day)).getTime();
    }

    private void insertData() throws Exception {
        query.insert(date(1984, 4, 26), "Brent");
        query.insert(date(1973, 8, 9), "John");
        query.insert(date(1967, 4, 1), "Earl");
    }

    public void testList() throws Exception {
        insertData();

        final List<Birthday> birthdays = query.selectList();

        assertNotNull(birthdays);
        assertEquals(birthdays.size(), 3);

        assertTrue(birthdays.contains(new Birthday(date(1984, 4, 26), "Brent")));
        assertTrue(birthdays.contains(new Birthday(date(1973, 8, 9), "John")));
        assertTrue(birthdays.contains(new Birthday(date(1967, 4, 1), "Earl")));
    }

    public void testSet() throws Exception {
        // we expect only one of each out, so we insert 2
        insertData();
        insertData();

        final Set<Birthday> birthdays = query.selectSet();

        assertNotNull(birthdays);
        assertEquals(birthdays.size(), 3);

        assertTrue(birthdays.contains(new Birthday(date(1984, 4, 26), "Brent")));
        assertTrue(birthdays.contains(new Birthday(date(1973, 8, 9), "John")));
        assertTrue(birthdays.contains(new Birthday(date(1967, 4, 1), "Earl")));
    }

    public void testSortedSet() throws Exception {
        // we expect only one of each out, so we insert 2
        insertData();
        insertData();

        final SortedSet<Birthday> birthdays = query.selectSortedSet();

        assertNotNull(birthdays);
        assertEquals(birthdays.size(), 3);

        final Iterator<Birthday> iterator = birthdays.iterator();

        assertEquals(new Birthday(date(1967, 4, 1), "Earl"), iterator.next());
        assertEquals(new Birthday(date(1973, 8, 9), "John"), iterator.next());
        assertEquals(new Birthday(date(1984, 4, 26), "Brent"), iterator.next());
    }

    public void testVector() throws Exception {
        insertData();

        final Vector<Birthday> birthdays = query.selectVector();

        assertNotNull(birthdays);
        assertEquals(birthdays.size(), 3);

        assertTrue(birthdays.contains(new Birthday(date(1984, 4, 26), "Brent")));
        assertTrue(birthdays.contains(new Birthday(date(1973, 8, 9), "John")));
        assertTrue(birthdays.contains(new Birthday(date(1967, 4, 1), "Earl")));
    }

    public void testEmptyCollection() throws Exception {
        query.deleteAll();

        final List<Birthday> list = query.selectList();

        assertNotNull(list);

        assertTrue(list.isEmpty());
        assertTrue(list.size() == 0);

        final Vector<Birthday> vector = query.selectVector();

        assertNotNull(vector);

        assertTrue(vector.isEmpty());
        assertTrue(vector.size() == 0);
    }

}
