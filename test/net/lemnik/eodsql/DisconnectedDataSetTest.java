package net.lemnik.eodsql;

/**
 *
 * @author Jason Morris
 */
public class DisconnectedDataSetTest extends AbstractDataSetTestObject {

    @Override
    protected DataSet<SimpleObject> getDataSet() throws Exception {
        return query.getDisconnected();
    }

}
