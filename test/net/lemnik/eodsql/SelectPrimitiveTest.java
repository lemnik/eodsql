package net.lemnik.eodsql;

import java.util.Random;

/**
 *
 * @author jason
 */
public class SelectPrimitiveTest extends EoDTestCase {

    private static boolean tableCreated = false;

    private Data[] allData;

    private int maxInt = Integer.MIN_VALUE;

    private int minInt = Integer.MAX_VALUE;

    private double avgDouble = 0.0;

    PrimitiveQuery query = null;

    public SelectPrimitiveTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        query = QueryTool.getQuery(getConnection(), PrimitiveQuery.class);
        query.createPrimitiveTable();

        createData(query);
    }

    public void testMinInt() throws Throwable {
        assertEquals(this.minInt, query.minInt());
    }

    public void testMaxInt() throws Exception {
        assertEquals(this.maxInt, query.maxInt());
    }

    public void testAvgDouble() throws Exception {
        assertEquals(this.avgDouble, query.averageDouble());
    }

    public void testEmptyArray() throws Exception {
        query.deleteAll();

        final int[] array = query.sortedInts();

        assertNotNull(array);
        assertEquals(0, array.length);
    }

    @Override
    protected void tearDown() throws Exception {
        query.dropPrimitiveTable();
        query = null;

        super.tearDown();
    }

    private void createData(PrimitiveQuery query) {
        double doubleValues = 0.0;
        allData = new Data[100];

        Random random = new Random();

        for(int i = 0; i < allData.length; i++) {
            allData[i] = new Data(random.nextInt(), random.nextDouble());
            query.insert(allData[i].intValue, allData[i].doubleValue);

            if(allData[i].intValue < minInt) {
                minInt = allData[i].intValue;
            }

            if(allData[i].intValue > maxInt) {
                maxInt = allData[i].intValue;
            }

            doubleValues += allData[i].doubleValue;
        }

        avgDouble = doubleValues / (double)allData.length;
    }

    private static class Data {

        public int intValue;

        public double doubleValue;

        public Data(int intValue, double doubleValue) {
            this.intValue = intValue;
            this.doubleValue = doubleValue;
        }

    }
}
