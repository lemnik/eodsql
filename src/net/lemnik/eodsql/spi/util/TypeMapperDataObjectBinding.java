package net.lemnik.eodsql.spi.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.lemnik.eodsql.TypeMapper;
import net.lemnik.eodsql.EoDException;

/**
 * Created on 2008/06/13
 * @author Jason Morris
 */
class TypeMapperDataObjectBinding<T> extends DataObjectBinding<T> {
    private TypeMapper<T> mapper;

    @SuppressWarnings("unchecked")
    TypeMapperDataObjectBinding(
            final TypeMapper<T> mapper,
            final Class<T> type) {
        
        setObjectType(type);
        super.setBindingType(BindingType.FIRST_COLUMN_BINDING);
        this.mapper = mapper;
    }

    @Override
    public T newInstance() throws EoDException {
        return null;
    }

    @Override
    public boolean isRubberstampCapable() {
        return false;
    }

    @Override
    public boolean isUpdateCapable() {
        return true;
    }

    @Override
    public boolean setBindingType(BindingType bindingType) {
        return false;
    }

    @Override
    public T unmarshall(final ResultSet row)
            throws SQLException,
            EoDException {

        return mapper.get(row, 1);
    }

    @Override
    public void unmarshall(
            final ResultSet row,
            final T into)
            throws SQLException,
            EoDException {
        
        throw new UnsupportedOperationException();
    }

    @Override
    public void marshall(
            final T from,
            final ResultSet results)
            throws SQLException,
            EoDException {

        mapper.set(results, 1, from);
    }

}
