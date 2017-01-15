package net.lemnik.eodsql.spi.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.lemnik.eodsql.impl.ExceptionTranslationUtils;

/**
 * Created on 2008/06/15
 * 
 * @author Jason Morris
 */
class SingleRowResultSetWrapper<T> extends AbstractResultSetWrapper<T, T> {
    SingleRowResultSetWrapper(final DataObjectBinding<T> binding) {
	super(binding);
    }

    @Override
    public T wrap(final ResultSet results) throws SQLException {
	if (results.next()) {
	    final T wrapped = binding.unmarshall(results);
	    if (results.next()) {
		throw ExceptionTranslationUtils.uniqueResultExpected();
	    }
	    return wrapped;
	} else {
	    return null;
	}
    }

}
