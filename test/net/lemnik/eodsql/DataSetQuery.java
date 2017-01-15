package net.lemnik.eodsql;

import java.util.Collection;

/**
 * 
 * @author jason
 */
public interface DataSetQuery extends BaseQuery {

    @Update("CREATE TABLE objects ("
    + "id CHAR(36), "
    + "data VARCHAR(100),"
    + "index INTEGER)")
    public void createObjectsTable();

    @Update("DROP TABLE objects")
    public void dropTable();

    @Update("INSERT INTO objects (id, data, index) "
    + "VALUES(?{1.id}, ?{1.data}, ?{1.order})")
    public void insert(SimpleObject object);

    @Update(sql = "INSERT INTO objects (id, data, index) "
    + "VALUES(?{1.id}, ?{1.data}, ?{1.order})", batchUpdate = true)
    public void insertBatchArray(SimpleObject[] object);

    @Update(sql = "INSERT INTO objects (id, data, index) "
    + "VALUES(?{1.id}, ?{1.data}, ?{1.order})", batchUpdate = true)
    public void insertBatchCollection(Collection<SimpleObject> object);

    @Update(sql = "INSERT INTO objects (id, data, index) "
    + "VALUES(?{1.id}, ?{2}, ?{1.order})", batchUpdate = true)
    public void insertBatchCollectionUniformData(Collection<SimpleObject> object, String data);

    @Update(sql = "INSERT INTO objects (id, index) "
    + "VALUES(?{2.id}, ?{1})", batchUpdate = true)
    public void insertBatchCollectionUniformData(int index, Collection<SimpleObject> object);

    @Select(sql = "SELECT * FROM objects ORDER BY index", disconnected = true)
    public DataSet<SimpleObject> getDisconnected();

    @Select("SELECT * FROM objects ORDER BY index")
    public DataSet<SimpleObject> getConnected();

    @Select(sql = "SELECT * FROM objects ORDER BY index", readOnly = false)
    public DataSet<SimpleObject> getWritable();
}
