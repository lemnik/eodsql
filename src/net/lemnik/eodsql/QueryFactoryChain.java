package net.lemnik.eodsql;

import java.sql.Connection;

import java.util.List;
import java.util.Iterator;
import java.util.Collections;

import java.util.concurrent.CopyOnWriteArrayList;

import javax.sql.DataSource;

import net.lemnik.eodsql.impl.DefaultQueryFactory;
import net.lemnik.eodsql.spi.QueryFactory;

/**
 * The {@code QueryFactoryChain} is responsible for creating the
 * implementations of {@link BaseQuery} object. The {@code QueryFactoryChain}
 * holds a list of the different {@code QueryFactory} implementations
 * available. The {@code QueryFactoryChain} has special reentrant behaviour
 * to cater for {@code QueryFactory} implementations that create delegating
 * (proxy) implementations. If a {@code QueryFactory} currently under
 * construction creates a {@code BaseQuery} implementation, the
 * {@code QueryFactoryChain} will only look at {@code QueryFactories} that
 * appear after the current one.
 *
 * @author Jason Morris
 */
class QueryFactoryChain {

    /**
     * This {@code ThreadLocal} is used to keep track of the current
     * {@code QueryFactory} during the {@code create} methods. Each time
     * a {@code create} method is re-entrantly called (ie: {@code create}
     * is called by a thread that is already calling {@code create}) we
     * will only attempt to use factories that appear in the chain after
     * the one currently being used.
     */
    private final ThreadLocal<CreateContext> chain =
            new ThreadLocal<CreateContext>() {

                @Override
                protected CreateContext initialValue() {
                    return new CreateContext(factories.iterator());
                }

            };

    /**
     * This is the global registry of {@code QueryFactory} objects. By
     * default this list is populated by adding the {@code QueryFactory}
     * objects to the begining of the {@code List}.
     */
    private final List<QueryFactory> factories;

    /**
     * Create a new {@code QueryFactoryChain}, the {@link QueryTool} class
     * will create one static instance of a {@code QueryFactoryChain}.
     * This constructor will also attempt to populate the
     * {@code QueryFactoryChain} with the {@link DefaultQueryFactory} and
     * any {@code QueryFactory} registered with a {@code ServiceLoader}
     * (assuming we are running on a Java VM 1.6 or higher).
     *
     * @see ServiceUtil
     */
    QueryFactoryChain() {
        // we use a CopyOnWriteArrayList to keep the structure
        // memory efficient over time (an array instead of links),
        // and to keep the expense constant (prepending objects is
        // as expensive as appending them would normally be).
        factories = new CopyOnWriteArrayList<QueryFactory>();

        // Add the default factory first, this is generally all thats needed
        // but because it's so generic we add it in first. Note that we
        // use the QueryFactory.add method and not List.add, since our
        // add implementation prepends the QueryFactory objects to the list.
        // This means the DefaultQueryFactory will always be the last one in
        // the List of QueryFactory objects.
        add(new DefaultQueryFactory());

        // Use the ServiceUtil in case we are running Java 1.5
        for(final QueryFactory factory : ServiceUtil.load(QueryFactory.class)) {
            add(factory);
        }
    }

    /**
     * Use this {@code QueryFactoryChain} to create a new {@code BaseQuery}
     * implementation. The implementation of this method is to start at the
     * begining of the chain, and for each subsequent re-entrant call, to
     * walk further down the chain. Each invokation will return when a
     * {@code QueryFactory} is found that
     * {@link QueryFactory#canConstruct(Class) can construct} an
     * implementation the requested query type.
     *
     * @param <T> the generic type of query to create
     * @param ds the {@code DataSource} to back the created query with
     * @param query the class type instance of the query to implement
     * @param loader the {@code ClassLoader} to use to create the query type
     * @return an instance of the requested query, backed by the given
     *      {@code DataSource}, or {@literal null} if the query couldn't
     *      be created by any registered {@code QueryFactory}
     * @throws InvalidQueryException
     */
    <T extends BaseQuery> T create(
            final DataSource ds,
            final Class<T> query,
            final ClassLoader loader)
            throws InvalidQueryException {

        if(factories.size() == 1) {
            // This is a short-path for when only one
            // QueryFactory is registered (will almost always be
            // the case, since most of the time only
            // DefaultQueryFactory is used)
            final QueryFactory factory = factories.get(0);

            if(factory.canConstruct(query)) {
                return factory.construct(ds, query, loader);
            } else {
                return null;
            }
        } else {
            T queryImpl = null;

            // Fetch the current CreateContext, the ThreadLocal will
            // create a new one for us if there isn't one already
            final CreateContext context = chain.get();

            // we count the number of entries to help the GC later on
            context.incEntries();

            try {
                final Iterator<QueryFactory> iterator = context.iterator;

                // Continue down the Iterator until we find QueryFactory
                // to create the new instance for us
                while(iterator.hasNext()) {
                    final QueryFactory factory = iterator.next();

                    if(factory.canConstruct(query)) {
                        queryImpl = factory.construct(ds, query, loader);
                        break;
                    }
                }
            } finally {
                // cleanup after ourselves
                if(context.decEntries() == 0) {
                    chain.remove();
                }
            }

            return queryImpl;
        }
    }

    /**
     * This implementation works exactly the same way as the
     * {@link #create(DataSource, Class, ClassLoader)} method,
     * but backs it's query instance with a single {@code Connection}
     * instead of a {@code DataSource}.
     *
     * @param <T> the generic type of query to create
     * @param context the {@code Connection} to back the created query with
     * @param query the class type instance of the query to implement
     * @param loader the {@code ClassLoader} to use to create the query type
     * @return an instance of the requested query, backed by the given
     *      {@code DataSource}, or {@literal null} if the query couldn't
     *      be created by any registered {@code QueryFactory}
     * @throws InvalidQueryException
     */
    <T extends BaseQuery> T create(
            final Connection connection,
            final Class<T> query,
            final ClassLoader loader)
            throws InvalidQueryException {

        if(factories.size() == 1) {
            // This is a short-path for when only one
            // QueryFactory is registered (will almost always be
            // the case, since most of the time only
            // DefaultQueryFactory is used)
            final QueryFactory factory = factories.get(0);

            if(factory.canConstruct(query)) {
                return factory.construct(connection, query, loader);
            } else {
                return null;
            }
        } else {
            final CreateContext context = chain.get();
            context.incEntries();

            T queryImpl = null;

            try {
                final Iterator<QueryFactory> iterator = context.iterator;

                while(iterator.hasNext()) {
                    final QueryFactory factory = iterator.next();

                    if(factory.canConstruct(query)) {
                        queryImpl = factory.construct(connection, query, loader);
                        break;
                    }
                }
            } finally {
                // cleanup after ourselves
                if(context.decEntries() == 0) {
                    chain.remove();
                }
            }

            return queryImpl;
        }
    }

    void add(final QueryFactory factory) {
        // we add factories to the begining of the list
        // this means the DefaultQueryFactory will always appear last
        factories.add(0, factory);
    }

    void remove(final QueryFactory factory) {
        // remove /all/ instances of the given factory
        factories.removeAll(Collections.singleton(factory));
    }

    private static class CreateContext {

        private final Iterator<QueryFactory> iterator;

        private int entries = 0;

        public CreateContext(final Iterator<QueryFactory> iterator) {
            assert iterator != null;

            this.iterator = iterator;
        }

        public int incEntries() {
            return ++entries;
        }

        public int decEntries() {
            return --entries;
        }

    }
}
