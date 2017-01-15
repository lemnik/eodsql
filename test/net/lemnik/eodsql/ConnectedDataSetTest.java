package net.lemnik.eodsql;

/**
 *
 * @author Jason Morris
 */
public class ConnectedDataSetTest extends AbstractDataSetTestObject {

    @Override
    protected DataSet<SimpleObject> getDataSet() throws Exception {
        return query.getConnected();
    }

}
