package net.lemnik.eodsql;

/**
 * <p>
 * <code>DataSet</code> objects that are connected to their
 * <code>ResultSet</code>s may use a <code>DataSetCache</code>
 * to maintain an in-memory cache of the rows that have been fetched.
 * The primary purpose of a <code>DataSetCache</code> is to maintain idetifier
 * equality for the fetched rows (ie: <code>row1 == row1</code>),
 * therefore the {@link ArrayDataSetCache default implementation}
 * is a <i>weak</i> cache (implemented with
 * {@link java.lang.ref.WeakReference}).
 * </p><p>
 * A <code>DataSetCache</code> is expected to be zero-based
 * (like a {@link java.util.List} or {@link DataSet}).
 * </p>
 *
 * @author Jason Morris
 * 
 * @param <T> the type being cached
 * @see Select#cache()
 * @see Call#cache()
 * @see DataSet
 */
public interface DataSetCache<T> {
    /**
     * Initialize this <code>DataSetCache</code>, which will be used to cache
     * rows for the {@link DataSet} specified here. There is generally no reason
     * to store the parent <code>DataSet</code>, but it is given anyway.
     *
     *@param dataSet the <code>DataSet</code> that will be using this <code>DataSetCache</code>
     */
    void init(DataSet<T> dataSet);
    
    /**
     *<p>
     * A quick check to see if the specified row is currently cached. This method does
     * <b>not</b> guarentee that {@link #getObject(int) getObject} will return a non-null
     * value, but it suggests that it probably won't. If this method returns <code>false</code>
     * however, it is assumed that <code>getObject</code> will return a null value, and that
     * the object should be re-fetched from the database.
     *</p><p>
     * The <code>row</code> passed into this method will never be negative, but any other value
     * is possible. The cache is expected to be zero-based.
     *</p>
     *
     *@param row the row number to check
     *@return <code>true</code> if the object is probably cached
     */
    boolean isCached(int row);
    
    /**
     * Returns the object at the specified row. If the row is not found in the cache, this method
     * may return <code>null</code>. If the return-value from this method is <code>null</code>,
     * the row will be fetched from the database.
     *
     *@param row the row to look for
     *@return the value cached in the row
     *@see #isCached(int)
     */
    T getObject(int row);
    
    /**
     * Sets the value of a row in this cache. This is generally called directly after a call to
     * {@link #isCached(int)} or {@link #getObject(int)} to set the value of the specified row.
     * The <code>object</code> value given here will <i>never</i> be null.
     *
     *@param row the row index
     *@param object the value of the row
     */
    void setObject(int row, T object);
    
    /**
     * Free's up any resources used by this <code>DataSetCache</code>. This method should also make a
     * best effort to flag any used resources for garbage-colletion. No method in this <code>DataSetCache</code>
     * will be used after this method is called.
     */
    void destroy();
}
