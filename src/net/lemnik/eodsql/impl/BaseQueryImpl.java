package net.lemnik.eodsql.impl;

import java.lang.annotation.Annotation;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;

import java.util.concurrent.locks.ReentrantLock;

import javax.sql.DataSource;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.InvalidQueryException;

import net.lemnik.eodsql.spi.MethodImplementation;
import net.lemnik.eodsql.spi.MethodImplementationFactory;

import net.lemnik.eodsql.spi.Context;
import net.lemnik.eodsql.spi.Resource;

/**
 * Created on 2008/06/26
 * @author Jason Morris
 */
class BaseQueryImpl implements InvocationHandler {

    protected Map<Method, Callable> methods = new HashMap<Method, Callable>();

    protected ConnectionSource connectionSource;

    BaseQueryImpl(
            final ConnectionSource connectionSource,
            final Class<? extends BaseQuery> clazz) {

        this(connectionSource, clazz, BaseQuery.class);
    }

    BaseQueryImpl(
            final ConnectionSource connectionSource,
            final Class<? extends BaseQuery> clazz,
            final Class<? extends BaseQuery> baseInterface) {

        this.connectionSource = connectionSource;
        createImplementations(clazz, baseInterface);
    }

    protected void close() throws SQLException {
        connectionSource.close();
    }

    protected boolean isClosed() {
        try {
            return connectionSource.isClosed();
        } catch(SQLException ex) {
            // Cannot use exception translator here as one cannot use closed connections for that.
            throw new EoDException("isClosed", ex);
        }
    }

    private Set<Class> getParentInterfaces(final Class<?> base) {
        final Set<Class> output = new HashSet<Class>();
        output.add(base);

        for(final Class<?> clazz : base.getInterfaces()) {
            output.add(clazz);
        }

        return output;
    }

    @SuppressWarnings("unchecked")
    private MethodImpl getMethodImpl(final Method method) {
        final Annotation[] annotations = method.getAnnotations();

        MethodImplementationFactory factory = null;
        Annotation factoryAnnotation = null;

        for(final Annotation annotation : annotations) {
            final MethodImplementationFactory annotationFactory =
                    QueryTool.getMethodImplementationFactory(annotation.annotationType());

            if(annotationFactory != null) {
                if(factory != null) {
                    throw new InvalidQueryException(
                            "All methods in a BaseQuery must have exactly "
                            + "one Method Implementation annotation.", method);
                } else {
                    factory = annotationFactory;
                    factoryAnnotation = annotation;
                }
            }
        }

        if(factory == null) {
            throw new InvalidQueryException(
                    "All methods in a BaseQuery must have an "
                    + "annotation with a matching MethodImplementationFactory.",
                    method);
        }

        return new MethodImpl(
                factory.createImplementation(method),
                factoryAnnotation);
    }

    protected void createImplementations(
            final Class<? extends BaseQuery> clazz,
            final Class<? extends BaseQuery> baseInterface) {

        final Set<Class> parents = getParentInterfaces(baseInterface);

        for(final Method method : clazz.getMethods()) {
            if(parents.contains(method.getDeclaringClass())) {
                continue;
            }

            methods.put(method, getMethodImpl(method));
        }

        try {
            final Class<BaseQuery> baseQuery = BaseQuery.class;
            methods.put(baseQuery.getMethod("close"), new InvokeClose());
            methods.put(baseQuery.getMethod("isClosed"), new InvokeIsClosed());
        } catch(NoSuchMethodException noSuchMethodException) {
        } catch(SecurityException securityException) {
        }
    }

    public Object invoke(
            final Object proxy,
            final Method method,
            final Object[] args)
            throws Throwable {

        final Callable impl = methods.get(method);
        try {
            return impl.invoke(method, args);
        } catch(final RuntimeException runtimeException) {
            // we catch this, so that it's not caught in the catch(Exception)
            throw runtimeException;
        } catch(final Exception exception) {
            final Class<?>[] exceptions = method.getExceptionTypes();
            final Class<? extends Exception> exceptionClass = exception.getClass();

            // if the Exception is declared, throw it normally
            for(final Class<?> ex : exceptions) {
                if(ex.isAssignableFrom(exceptionClass)) {
                    throw exception;
                }
            }

            // if we got here, the Exception was not declared, wrap it in a RuntimeException
            throw ExceptionTranslationUtils.translateException(connectionSource, method, impl, exception);
        }
    }

    protected Context<Annotation> createContext(Annotation annotation, final Object[] args) {
        return new Context<Annotation>(annotation, args);
    }

    static interface Callable {

        public Object invoke(Method method, Object[] args) throws Throwable;
    }

    class InvokeClose implements Callable {

        public Object invoke(Method method, Object[] args) throws Throwable {
            close();
            return null;
        }
    }

    class InvokeIsClosed implements Callable {

        public Object invoke(Method method, Object[] args) throws Throwable {
            return isClosed();
        }
    }

    class MethodImpl implements Callable {

        private final MethodImplementation<Annotation> implementation;

        private final Annotation annotation;

        MethodImpl(
                final MethodImplementation<Annotation> implementation,
                final Annotation annotation) {

            this.implementation = implementation;
            this.annotation = annotation;
        }

        public Object invoke(
                final Method method,
                final Object[] args)
                throws Throwable {

            final Context<Annotation> context = createContext(annotation, args);

            final Resource<Connection> connection =
                    new ConnectionSourceConnectionResource(connectionSource);

            context.setResource(connection);

            try {
                implementation.invoke(context);
                return context.getReturnValue();
            } finally {
                if(context.isAutoclose()) {
                    context.close();
                }
            }
        }

        AbstractMethodImplementation<Annotation> getMethodImpl() {
            return (AbstractMethodImplementation<Annotation>)implementation;
        }
    }

    static interface ConnectionSource {

        public Connection getConnection() throws SQLException;

        public void releaseConnection(Connection connection) throws SQLException;

        public void close() throws SQLException;

        public boolean isClosed() throws SQLException;
    }

    static class SingleConnectionSource implements ConnectionSource {

        private final Connection connection;

        private final ReentrantLock lock = new ReentrantLock();

        SingleConnectionSource(final Connection connection) {
            this.connection = connection;
        }

        public Connection getConnection() throws SQLException {
            try {
                lock.lockInterruptibly();
            } catch(InterruptedException interruptedException) {
                throw (SQLException)(new SQLException().initCause(
                        interruptedException));
            }

            return connection;
        }

        public void releaseConnection(
                final Connection connection)
                throws SQLException {

            if(this.connection == connection) {
                lock.unlock();
            }
        }

        public void close() throws SQLException {
            connection.close();
        }

        public boolean isClosed() throws SQLException {
            return connection.isClosed();
        }
    }

    static class DataSourceConnectionSource implements ConnectionSource {

        private static final ThreadLocal<ConnectionUtil> CONNECTION_UTILS =
                new ThreadLocal<ConnectionUtil>();

        private final DataSource datasource;

        private final boolean autoCommit;

        // Dummy value to associate with an Object in the backing Map
        private static final Object PRESENT = new Object();

        private final Map<Connection, Object> connections = Collections.synchronizedMap(
                new IdentityHashMap<Connection, Object>());

        DataSourceConnectionSource(final DataSource datasource, final boolean autoCommit) {
            this.datasource = datasource;
            this.autoCommit = autoCommit;
        }

        @Override
        protected void finalize() throws Throwable {
            close();
            super.finalize();
        }

        public void close() throws SQLException {
            // when closing, we want exclusive access
            synchronized(connections) {
                final Iterator<Connection> it = connections.keySet().iterator();

                while(it.hasNext()) {
                    it.next().close();
                    it.remove();
                }
            }
        }

        public boolean isClosed() throws SQLException {
            return connections.isEmpty();
        }

        public Connection getConnection() throws SQLException {
            ConnectionUtil util = CONNECTION_UTILS.get();

            if(util == null || util.connection.isClosed()) {
                final Connection tmp = datasource.getConnection();
                try {
                    tmp.setAutoCommit(autoCommit);
                } catch(SQLException ex) {
                    throw ExceptionTranslationUtils.translateException(tmp, "setAutoCommit", "-", ex);
                }

                connections.put(tmp, PRESENT);

                util = new ConnectionUtil(tmp);
                util.count++;
                CONNECTION_UTILS.set(util);
            } else {
                util.count++;
            }

            return util.connection;
        }

        public void releaseConnection(final Connection connection)
                throws SQLException {

            if(connections.containsKey(connection)) {
                final ConnectionUtil util = CONNECTION_UTILS.get();

                if(--util.count <= 0) {
                    connections.remove(connection);
                    connection.close();
                    CONNECTION_UTILS.remove();
                }
            }
        }

        DataSource getDataSource() {
            return datasource;
        }

        private static class ConnectionUtil {

            private final Connection connection;

            private int count;

            ConnectionUtil(final Connection connection) {
                this.connection = connection;
                this.count = 0;
            }
        }
    }

    private static class ConnectionSourceConnectionResource implements Resource<Connection> {

        private final ConnectionSource connectionSource;

        private Connection connection;

        public ConnectionSourceConnectionResource(
                final ConnectionSource connectionSource)
                throws SQLException {

            this.connectionSource = connectionSource;
            this.connection = connectionSource.getConnection();
        }

        public Connection get() {
            return connection;
        }

        public boolean isClosed() {
            return connection == null;
        }

        public void close() throws SQLException {
            final Connection local = connection;
            connection = null;

            if(local != null) {
                connectionSource.releaseConnection(local);
            }
        }

        public Class<Connection> getResourceType() {
            return Connection.class;
        }
    }
}
