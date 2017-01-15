/*
 * Copyright Jason Morris 2008. All rights reserved.
 */
package net.lemnik.eodsql.mock;

import java.util.List;
import java.util.Collection;
import java.util.AbstractList;

import net.lemnik.eodsql.DataSet;

/**
 * <p>
 * </p><p>
 * Created on 18 Feb 2010
 * </p>
 *
 * @author Jason Morris
 */
public class MockDataSet<E> extends AbstractList<E> implements DataSet<E> {

    private final List<E> backing;

    private final boolean mutable;

    private boolean connected;

    private boolean closed = false;

    public MockDataSet(
            final List<E> backing,
            final boolean mutable,
            final boolean connected) {

        if(backing == null) {
            throw new NullPointerException("No backing List specified");
        }

        if(mutable && !connected) {
            throw new IllegalArgumentException(
                    "A DataSet cannot be mutable and disconnected.");
        }

        this.backing = backing;
        this.mutable = mutable;
        this.connected = connected;
    }

    private void checkWritable() {
        if(!mutable) {
            throw new RuntimeException("This DataSet is not writable.");
        }
    }

    public void close() {
        closed = true;
    }

    public boolean isConnected() {
        return connected && !closed;
    }

    public void disconnect() {
        if(mutable) {
            throw new IllegalArgumentException(
                    "Cannot disconnect a read-write DataSet");
        }

        connected = false;
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public E set(final int index, final E element) {
        checkWritable();
        return backing.set(index, element);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        checkWritable();
        return backing.retainAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        checkWritable();
        return backing.removeAll(c);
    }

    @Override
    public E remove(final int index) {
        checkWritable();
        return backing.remove(index);
    }

    @Override
    public boolean remove(final Object o) {
        checkWritable();
        return backing.remove(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return backing.lastIndexOf(o);
    }

    @Override
    public int indexOf(final Object o) {
        return backing.indexOf(o);
    }

    @Override
    public E get(final int index) {
        return backing.get(index);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return backing.containsAll(c);
    }

    @Override
    public boolean contains(final Object o) {
        return backing.contains(o);
    }

    @Override
    public void clear() {
        checkWritable();
        backing.clear();
    }

    @Override
    public boolean addAll(
            final int index,
            final Collection<? extends E> c) {

        checkWritable();
        return backing.addAll(index, c);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        checkWritable();
        return backing.addAll(c);
    }

    @Override
    public void add(
            final int index,
            final E element) {

        checkWritable();
        backing.add(index, element);
    }

    @Override
    public boolean add(final E e) {
        checkWritable();
        return backing.add(e);
    }

}
