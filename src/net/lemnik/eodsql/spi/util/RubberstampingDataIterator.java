package net.lemnik.eodsql.spi.util;

import java.sql.SQLException;
import net.lemnik.eodsql.spi.Context;

/**
 * Created on 2008/07/23
 * @author Jason Morris
 */
class RubberstampingDataIterator<T> extends AbstractDataIterator<T> {
    private T object = null;
    
    public RubberstampingDataIterator(
            final Context<?> context,
            final DataObjectBinding<T> binding) {
        
        super(context, binding);
        
        object = binding.newInstance();
    }

    @Override
    protected T unmarshal() throws SQLException {
        binding.unmarshall(results, object);
        return object;
    }

}
