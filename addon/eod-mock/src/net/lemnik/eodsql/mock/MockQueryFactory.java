package net.lemnik.eodsql.mock;

import java.sql.Connection;

import java.util.Map;
import java.util.HashMap;

import javax.sql.DataSource;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.InvalidQueryException;
import net.lemnik.eodsql.QueryTool;

import net.lemnik.eodsql.spi.QueryFactory;

/**
 * <p>
 * </p><p>
 * Created on 02 Feb 2010
 * </p>
 *
 * @author Jason Morris
 */
public class MockQueryFactory implements QueryFactory {

    private final Map<Class<? extends BaseQuery>, Class<? extends BaseQuery>> replacements =
            new HashMap<Class<? extends BaseQuery>, Class<? extends BaseQuery>>();

    public MockQueryFactory() {
        QueryTool.setDefaultDataSource(new MockDataSource());
    }

    public boolean canConstruct(
            final Class<? extends BaseQuery> query) {

        if(replacements.containsKey(query)) {
            return true;
        } else {
            try {
                Class.forName(query.getName() + "Mock");
            } catch(final ClassNotFoundException cnfe) {
                return false;
            }
        }

        return true;
    }

    public <T extends BaseQuery> T construct(
            final Connection connection,
            final Class<T> query,
            final ClassLoader loader)
            throws InvalidQueryException {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseQuery> T construct(
            final DataSource dataSource,
            final Class<T> query,
            final ClassLoader loader)
            throws InvalidQueryException {

        Class<? extends BaseQuery> type = null;

        if(replacements.containsKey(query)) {
            type = replacements.get(query);
        } else {
            try {
                type = (Class<? extends BaseQuery>)Class.forName(
                        query.getName() + "Mock");
            } catch(final ClassNotFoundException cnfe) {
                throw new InvalidQueryException(
                        "No such class: " + query.getName() + "Mock",
                        cnfe);
            }
        }

        try {
            return (T)type.newInstance();
        } catch(final InstantiationException ie) {
            throw new InvalidQueryException(
                    "Couldn't create instance of " + type.getName(),
                    ie);
        } catch(final IllegalAccessException iae) {
            throw new InvalidQueryException(
                    "Couldn't create instance of " + type.getName(),
                    iae);
        }
    }

}
