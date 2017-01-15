package net.lemnik.eodsql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created on Aug 11, 2009
 * @author Jason Morris
 */
public class WritableDataSetTest extends AbstractDataSetTestObject {

    @Override
    protected DataSet<SimpleObject> getDataSet() throws Exception {
        return query.getWritable();
    }

    public void testStatementDatabaseSupport() throws Exception {
        final Connection connection = getConnection();
        final Statement statement = connection.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);

        assertEquals(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                statement.getResultSetType());
        
        assertEquals(
                ResultSet.CONCUR_UPDATABLE,
                statement.getResultSetConcurrency());

        final ResultSet results = statement.executeQuery("SELECT * FROM objects");

        assertNotNull(results);

        assertEquals(
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                results.getType());

        assertEquals(
                ResultSet.CONCUR_UPDATABLE,
                results.getConcurrency());

        assertTrue(results.next());

        results.moveToInsertRow();

        results.updateString("id", UUID.randomUUID().toString());
        results.updateString("data", "Hello World!");
        results.updateInt("index", 1024);
        results.insertRow();
        results.moveToCurrentRow();
    }

    public void testDataSetAddition() throws Exception {
        final List<SimpleObject> validation = new ArrayList<SimpleObject>();

        insertData(validation);

        final DataSet<SimpleObject> objects = getDataSet();

        for(int i = 0; i < 100; i++) {
            final SimpleObject obj = newSimpleObject(i + 1000);

            validation.add(obj);
            objects.add(obj);
        }

        assertDataSetEquals(validation, objects);
    }

    public void testDataSetModification() throws Exception {
        final List<SimpleObject> validation = new ArrayList<SimpleObject>();

        insertData(validation);

        final DataSet<SimpleObject> objects = getDataSet();

        assertDataSetEquals(validation, objects);

        for(int i = 0; i < objects.size(); i++) {
            final SimpleObject dbo = objects.get(i);
            dbo.data = "mod[" + i + "]";

            objects.set(i, dbo);
            validation.set(i, dbo);
        }

        assertDataSetEquals(validation, objects);
    }

}
