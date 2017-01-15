package net.lemnik.eodsql.spi.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Collection;
import java.util.AbstractList;

import net.lemnik.eodsql.DataSet;

import net.lemnik.eodsql.spi.Context;
import net.lemnik.eodsql.spi.Resource;

/**
 *
 * @author jason
 */
class DisconnectedDataSet<T> extends AbstractList<T> implements DataSet<T> {
    
    private final Object[] content;

    /** Creates a new instance of DisconnectedDataSet */
    public DisconnectedDataSet(
            final DataObjectBinding mapper,
            final Context<?> context)
            throws SQLException {

        final Resource<ResultSet> resultsResource =
                context.getResource(ResultSet.class);
        final ResultSet results = resultsResource.get();
        
        results.last();

        final int size = results.getRow();
        content = new Object[size];
        
        results.beforeFirst();

        int i = 0;
        while(results.next()) {
            content[i++] = mapper.unmarshall(results);
        }
    }

    public void close() {
    }

    public boolean isConnected() {
        return false;
    }

    public void disconnect() {
    }

    @Override
    public int size() {
        return content.length;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(final int index) {
        if(index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        return (T)content[index];
    }

    // <editor-fold defaultstate="collapsed" desc=" Unsupported Operations ">
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }
    // </editor-fold>
}
