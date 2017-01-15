package net.lemnik.eodsql;

/**
 *
 * @author Jason Morris
 */
public class LateDisconnectedDataSetTest extends AbstractDataSetTestObject {

    @Override
    protected DataSet<SimpleObject> getDataSet() throws Exception {
        final DataSet<SimpleObject> ds = query.getConnected();
        ds.disconnect();

        return ds;
    }
    
}
