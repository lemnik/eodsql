package net.lemnik.eodsql.spi.util;

import java.sql.SQLException;
import net.lemnik.eodsql.spi.Context;

/**
 * Created on 2008/07/23
 * @author Jason Morris
 */
class DefaultDataIterator<T> extends AbstractDataIterator<T> {
    public DefaultDataIterator(
            final Context<?> context,
            final DataObjectBinding<T> binding) {
        
        super(context, binding);
    }
    
    
    @Override
    protected T unmarshal() throws SQLException {
        return binding.unmarshall(results);
    }
    
}
