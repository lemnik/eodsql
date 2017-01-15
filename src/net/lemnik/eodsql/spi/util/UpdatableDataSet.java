package net.lemnik.eodsql.spi.util;

import java.sql.SQLException;

import java.util.Collection;

import net.lemnik.eodsql.spi.Context;

import net.lemnik.eodsql.DataSetCache;

/**
 *
 * @author Jason Morris
 */
class UpdatableDataSet<T> extends ConnectedDataSet<T> {

    UpdatableDataSet(
            final DataObjectBinding<T> binding,
            final Context context,
            final DataSetCache<T> cache) {

        super(binding, context, cache);
    }

    @Override
    public void disconnect() {
        throw new UnsupportedOperationException(
                "Updatable DataSet cannot be disconnected.");
    }

    @Override
    public boolean add(final T e) {
        try {
            results.moveToInsertRow();
            binding.marshall(e, results);
            results.insertRow();
            results.moveToCurrentRow();

            setSize(null);
        } catch(final SQLException ex) {
            throw new RuntimeException(ex);
        }

        return true;
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        if(c.isEmpty()) {
            return false;
        } else {
            for(final T o : c) {
                add(o);
            }

            return true;
        }
    }

    @Override
    public T remove(final int index) {
        final T value = get(index);

        try {
            results.absolute(index + 1);
            results.deleteRow();
            setSize(null);
        } catch(final SQLException ex) {
            throw new RuntimeException(ex);
        }

        return value;
    }

    @Override
    public T set(final int index, final T element) {
        final T oldValue = get(index);

        try {
            results.absolute(index + 1);
            binding.marshall(element, results);
            results.updateRow();
        } catch(final SQLException ex) {
            throw new RuntimeException(ex);
        }

        return oldValue;
    }

}
