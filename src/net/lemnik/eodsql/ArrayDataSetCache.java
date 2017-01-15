package net.lemnik.eodsql;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 *<p>
 * An implementation of {@link DataSetCache} that uses an array of
 * {@link java.lang.ref.WeakReference}'s to store the row values.
 * This is the default cache implementation.
 *</p><p>
 * While fairly quick, an <code>ArrayDataSetCache</code> does hold
 * references for as long as you do. If you do not need a cache for
 * identity equality an {@code ArrayDataSetCache} is probably overkill.
 * For large <code>DataSet</code>'s it is advised that you either look
 * at a {@link NullDataSetCache}, or into using a {@link DataIterator}
 * instead of a {@code DataSet}.
 *</p>
 *
 * @param <T> the row data-type to cache
 * @author Jason Morris
 * @see NullDataSetCache
 * @see DataIterator
 */
public class ArrayDataSetCache<T> implements DataSetCache<T> {

    /**
     * The starting size of an {@code ArrayDataSetCache}. This may be
     * tuned differently in the future, but for now it's set to
     * a nice round {@literal 10}. This determines the exact number of
     * {@code WeakReference} elements in the array when a new
     * {@code ArrayDataSetCache} is created.
     */
    private static final int DEFAULT_SIZE = 10;

    /**
     * <p>
     * Each {@code ArrayDataSetCache} maintains it's own
     * {@code ReferenceQueue} object. This queue is polled
     * for stale entries each time an object is fetched or
     * added to the cache.
     * </p><p>
     * A note on the {@code ReferenceQueue} implementation: unfortunately
     * {@code ReferenceQueue} is not the most high performance structure
     * under some conditions in that it will always aquire it's internal
     * queue lock before returning from a {@link ReferenceQueue#poll()}.
     * This means that even if the queue is empty, we still go through
     * a lock. However since the default {@link DataSet} implementations
     * are not concurrenly safe to use, this should not impact performance
     * (since the lock will almost never be under contention).
     * </p>
     */
    private ReferenceQueue<T> queue = new ReferenceQueue<T>();

    /**
     * This is our array of {@code WeakReference} objects. In actual fact
     * we extend the {@code WeakReference} class to carry the index in
     * this array (so that we can release the {@code WeakReference} object
     * when it's referent object is no longer in use).
     *
     * @see IndexedWeakReference
     */
    private WeakReference<?>[] cache = new WeakReference<?>[DEFAULT_SIZE];

    /**
     * Default constructor for {@code ArrayDataSetCache}.
     * This will create an internal {@link WeakReference} cache of a
     * preset size, ready for objects to be cached.
     */
    public ArrayDataSetCache() {
    }

    private void ensureCapacity(final int newCapacity) {
        final int len = cache.length;
        if(len < newCapacity) {
            final WeakReference<?>[] newCache =
                new WeakReference<?>[Math.max((len * 3) / 2 + 1, newCapacity)];
            System.arraycopy(cache, 0, newCache, 0, len);
            cache = newCache;
        }
    }

    /**
     * Remove any stale entries from this cache. This method polls the
     * {@link #queue ReferenceQueue} for any entries that are no
     * longer in use by the client application, we use thier stored
     * indices to clear them from the array (and allow the GC to do
     * it's work).
     *
     * @see IndexedWeakReference
     */
    @SuppressWarnings("unchecked")
    private void removeOldEntries() {
        IndexedWeakReference ref = null;

        while((ref = (IndexedWeakReference)queue.poll()) != null) {
            // allow the collector to GC the reference object
            cache[ref.getIndex()] = null;
        }
    }

    public void init(final DataSet<T> dataSet) {
    }

    public boolean isCached(final int row) {
        return (row < cache.length &&
                cache[row] != null &&
                cache[row].get() != null);
    }

    public T getObject(final int row) {
        removeOldEntries();

        if(isCached(row)) {
            @SuppressWarnings("unchecked")
            final T value = (T)cache[row].get();
            return value;
        }

        return null;
    }

    public void setObject(final int row, final T object) {
        removeOldEntries();
        ensureCapacity(row + 1);
        cache[row] = new IndexedWeakReference<T>(row, object, queue);
    }

    public void destroy() {
        cache = null;
        queue = null;
    }

    private static class IndexedWeakReference<T> extends WeakReference<T> {
        private final int index;

        IndexedWeakReference(
                final int index,
                final T obj,
                final ReferenceQueue<T> queue) {
            
            super(obj, queue);
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

    }

}
