package net.lemnik.eodsql.spi.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.AbstractList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.DataSetCache;

import net.lemnik.eodsql.spi.Context;
import net.lemnik.eodsql.spi.Resource;

/**
 *
 * @author jason
 */
class ConnectedDataSet<T> implements DataSet<T> {

    protected final Context context;

    protected final ResultSet results;

    protected final DataObjectBinding<T> binding;

    protected DataSetCache<T> cache;

    private Integer size = null;

    private boolean disconnected = false;

    /** Creates a new instance of ConnectedDataSet */
    public ConnectedDataSet(
            final DataObjectBinding<T> binding,
            final Context<?> context,
            final DataSetCache<T> cache) {

        this.binding = binding;
        this.context = context;
        this.cache = cache;

        final Resource<ResultSet> resultsResource =
                context.getResource(ResultSet.class);

        this.results = resultsResource.get();

        cache.init(this);
    }

    protected void setSize(final Integer size) {
        this.size = size;
    }

    public void close() {
        try {
            if(!disconnected) {
                cache.destroy();
                cache = null;

                context.close();
                disconnected = true;
            }
        } catch(final SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isConnected() {
        return !disconnected;
    }

    public void disconnect() {
        if(!disconnected) {
            try {
                final List<T> tmp = getArrayList(false);
                setSize(tmp.size());

                close();
                disconnected = true;

                cache = new HardDataSetCache<T>(tmp);
            } catch(SQLException sqle) {
                throw new RuntimeException(sqle);
            }
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public T get(final int index) {
        if(index < 0) {
            throw new IndexOutOfBoundsException();
        }

        T value = null;

        if(cache.isCached(index)) {
            value = cache.getObject(index);
        }

        if(value == null) {
            try {
                if(results.absolute(index + 1)) {
                    value = binding.unmarshall(results);
                    cache.setObject(index, value);
                } else {
                    throw new IndexOutOfBoundsException(
                            "Index out of bounds: " + index);
                }
            } catch(final SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return value;
    }

    public Iterator<T> iterator() {
        return new IteratorImpl(0);
    }

    public ListIterator<T> listIterator() {
        return new IteratorImpl(0);
    }

    public ListIterator<T> listIterator(final int index) {
        return new IteratorImpl(index);
    }

    private List<T> getArrayList(final boolean addToCache) throws SQLException {
        if(cache instanceof HardDataSetCache) {
            return ((HardDataSetCache<T>)cache).cache;
        } else {
            results.beforeFirst();
            final ArrayList<T> tmp = new ArrayList<T>();

            int row = 0;

            while(results.next()) {
                T value = null;

                if(cache.isCached(row)) {
                    value = cache.getObject(row);
                }

                if(value == null) {
                    value = binding.unmarshall(results);

                    if(addToCache) {
                        cache.setObject(row, value);
                    }
                }

                tmp.add(value);
                row++;
            }

            tmp.trimToSize();

            return tmp;
        }
    }

    public boolean contains(final Object o) {
        if(o != null) {
            for(final T v : this) {
                if(o.equals(v)) {
                    return true;
                }
            }
        }

        return false;
    }

    public int indexOf(final Object o) {
        if(o != null) {
            int index = 0;

            for(final T v : this) {
                if(o.equals(v)) {
                    return index;
                }

                index++;
            }
        }

        return -1;
    }

    /**
     * <p>
     * This implementation makes use of the {@link ResultSet#previous()}
     * method to navagate throught the data backwards. This method is
     * used in preference to {@link #get(int)} to avoid over-using
     * {@link ResultSet#absolute(int)} on a {@code ResultSet} that
     * may well have a server-side cursor.
     * </p><p>
     * This method does do reference equality before
     * {@link Object#equals(Object) object-equality}, thus a good
     * implementation of {@link DataSetCache} will generally aid
     * performance.
     * </p>
     *
     * @param o the {@code Object} to search the {@code DataSet} for
     * @return {@literal true} if this {@code DataSet} contains the given
     *      {@code Object} or one {@code equals} to it
     */
    public int lastIndexOf(final Object o) {
        if(o != null) {
            try {
                results.afterLast();
                int index = results.getRow();

                while(results.previous()) {
                    index--;

                    T value = null;

                    if(cache.isCached(index)) {
                        value = cache.getObject(index);
                    }

                    if(value == null) {
                        binding.unmarshall(results);
                        cache.setObject(index, value);
                    }

                    if(o == value || o.equals(value)) {
                        return index;
                    }
                }
            } catch(final SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return -1;
    }

    public List<T> subList(final int fromIndex, final int toIndex) {
        if(fromIndex < toIndex && fromIndex >= 0 && toIndex < size()) {
            return new SubList(fromIndex, toIndex);
        }

        throw new IndexOutOfBoundsException();
    }

    public boolean containsAll(final Collection<?> c) {
        final Iterator<?> e = c.iterator();

        while(e.hasNext()) {
            if(!contains(e.next())) {
                return false;
            }
        }

        return true;
    }

    public int size() {
        if(size == null) {
            try {
                results.last();
                size = results.getRow();
            } catch(SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        return size;
    }

    // <editor-fold defaultstate="collapsed" desc=" Iterator Implementation ">
    private class IteratorImpl implements ListIterator<T> {

        private int cursor = 0;

        IteratorImpl(int start) {
            cursor = start;
        }

        public boolean hasNext() {
            if(isEmpty()) {
                return false;
            }

            return cursor < size();
        }

        public T next() {
            if(hasNext()) {
                return get(cursor++);
            } else {
                throw new NoSuchElementException();
            }
        }

        public boolean hasPrevious() {
            return cursor > 0 && !isEmpty();
        }

        public T previous() {
            if(hasPrevious()) {
                return get(--cursor);
            } else {
                throw new NoSuchElementException();
            }
        }

        public int nextIndex() {
            return cursor;
        }

        public int previousIndex() {
            return cursor - 1;
        }

        public void remove() {
            ConnectedDataSet.this.remove(previousIndex());
        }

        public void set(final T e) {
            ConnectedDataSet.this.set(previousIndex(), e);
        }

        public void add(final T e) {
            ConnectedDataSet.this.add(previousIndex(), e);
        }

    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Disconnected methods ">
    public Object[] toArray() {
        disconnect();

        try {
            return getArrayList(false).toArray();
        } catch(SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <T> T[] toArray(final T[] a) {
        disconnect();

        try {
            return getArrayList(false).toArray(a);
        } catch(SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Unsupported methods ">
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean add(T e) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" HardDataSet implementation ">
    private static class HardDataSetCache<T> implements DataSetCache<T> {

        private final List<T> cache;

        private HardDataSetCache(final List<T> cache) {
            this.cache = cache;
        }

        public void init(final DataSet<T> dataSet) {
        }

        public boolean isCached(final int row) {
            return row < cache.size();
        }

        public T getObject(final int row) {
            return cache.get(row);
        }

        public void setObject(final int row, final T object) {
        }

        public void destroy() {
        }

    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" SubList implementation ">
    private class SubList extends AbstractList<T> {

        private final int from;

        private final int to;

        private SubList(final int from, final int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public T get(final int index) {
            final int row = index + from;

            if(row >= from && row < to) {
                return ConnectedDataSet.this.get(row);
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        @Override
        public int size() {
            return to - from;
        }

    }
    // </editor-fold>
}
