package net.lemnik.eodsql;

import java.util.ArrayList;
import java.util.List;
import net.lemnik.eodsql.LooseMappingQuery.CompleteRow;
import net.lemnik.eodsql.LooseMappingQuery.SubsetRow;

/**
 * Created on 2008/07/29
 * @author Jason Morris
 */
public class LooseMappingTest extends EoDTestCase {
    private LooseMappingQuery query;

    public LooseMappingTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        query = QueryTool.getQuery(getConnection(), LooseMappingQuery.class);
        query.create();
    }

    @Override
    protected void tearDown() throws Exception {
        query.drop();
        query = null;
        
        super.tearDown();
    }
    
    public void testLooseBinding() throws Exception {
        List<SubsetRow> expected = new ArrayList<SubsetRow>(100);
        
        for(int i = 0; i < 100; i++) {
            CompleteRow row = new CompleteRow();
            row.col1 = (int)(Math.random() * 100);
            row.col2 = (int)(Math.random() * 100);
            row.col3 = (int)(Math.random() * 100);
            row.col4 = (int)(Math.random() * 100);
            
            query.insert(row);
            expected.add(new SubsetRow(row.col2, row.col3));
        }
        
        SubsetRow[] found = query.select(); // if this passes, we're fine
        
        assertEquals(expected.size(), found.length);
        
        for(SubsetRow row : found) {
            assertTrue(row + " was not inserted into the database", expected.contains(row));
        }
    }
    
    public void testLooseBindingDataSet() throws Exception {
        List<SubsetRow> expected = new ArrayList<SubsetRow>(100);
        
        for(int i = 0; i < 100; i++) {
            CompleteRow row = new CompleteRow();
            row.col1 = (int)(Math.random() * 100);
            row.col2 = (int)(Math.random() * 100);
            row.col3 = (int)(Math.random() * 100);
            row.col4 = (int)(Math.random() * 100);
            
            query.insert(row);
            expected.add(new SubsetRow(row.col2, row.col3));
        }
        
        DataSet<SubsetRow> found = query.selectDataSet();
        
        assertEquals(expected.size(), found.size());
        
        for(SubsetRow row : found) {
            assertTrue(row + " was not inserted into the database", expected.contains(row));
        }
    }
    
    public void testLooseBindingDataIterator() throws Exception {
        List<SubsetRow> expected = new ArrayList<SubsetRow>(100);
        
        for(int i = 0; i < 100; i++) {
            CompleteRow row = new CompleteRow();
            row.col1 = (int)(Math.random() * 100);
            row.col2 = (int)(Math.random() * 100);
            row.col3 = (int)(Math.random() * 100);
            row.col4 = (int)(Math.random() * 100);
            
            query.insert(row);
            expected.add(new SubsetRow(row.col2, row.col3));
        }
        
        DataIterator<SubsetRow> found = query.selectDataIterator();
        
        for(SubsetRow row : found) {
            assertTrue(row + " was not inserted into the database", expected.contains(row));
        }
    }

}
