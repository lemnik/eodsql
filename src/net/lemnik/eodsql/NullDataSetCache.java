package net.lemnik.eodsql;

/**
 * An implementation of {@link DataSetCache} that does not cache rows.
 * This implementation can be used for very large {@code DataSet}s,
 * but it forces every call to "get()" to re-fetch the row
 * (and therefore reconstruct the {@code Object}).
 *
 * @author Jason Morris
 */
public class NullDataSetCache<T> implements DataSetCache<T> {

    public void init(final DataSet<T> dataSet) {
    }

    public boolean isCached(final int row) {
        return false;
    }

    public T getObject(final int row) {
        return null;
    }

    public void setObject(final int row, final T object) {
    }

    public void destroy() {
    }
    
}
