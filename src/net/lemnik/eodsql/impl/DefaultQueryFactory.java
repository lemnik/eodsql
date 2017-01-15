package net.lemnik.eodsql.impl;

import java.lang.annotation.Annotation;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;

import java.sql.Connection;

import javax.sql.DataSource;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.InvalidQueryException;

import net.lemnik.eodsql.spi.QueryFactory;
import net.lemnik.eodsql.spi.MethodImplementationFactory;

/**
 * The {@code DefaultQueryFactory} class is the standard implementation of the {@link QueryFactory}
 * interface. This class uses the {@link MethodImplementationFactory} objects registered
 * with the {@link QueryTool} to build up implementations of the {@link BaseQuery}s.
 * 
 * @author Jason Morris
 */
public class DefaultQueryFactory implements QueryFactory {
    static {
        DefaultTypeMappers.register();
        
        QueryTool.addMethodImplementationFactory(
                new SelectMethodImplementationFactory());
        QueryTool.addMethodImplementationFactory(
                new UpdateMethodImplementation.Factory());
        QueryTool.addMethodImplementationFactory(
                new CallMethodImplementation.Factory());

        // search out all of the SPI MethodImplementationFactory's
        try {
            @SuppressWarnings("unchecked")
            final Iterable<MethodImplementationFactory> loader =
                    (Iterable<MethodImplementationFactory>)Class.forName("java.util.ServiceLoader").getMethod("load", new Class[]{
                        Class.class
                    }).invoke(null, new Object[]{MethodImplementationFactory.class});

            for(final MethodImplementationFactory factory : loader) {
                @SuppressWarnings("unchecked")
                final MethodImplementationFactory<? extends Annotation> f = factory;
                QueryTool.addMethodImplementationFactory(f);
            }
        } catch(NoClassDefFoundError e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(IllegalArgumentException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(SecurityException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(IllegalAccessException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(InvocationTargetException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(NoSuchMethodException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(ClassNotFoundException e) {
            // ignore this... we are simply not running 1.6 or higher
        }
    }

    /**
     * The {@code DefaultQueryFactory} can construct any type of {@code BaseQuery}.
     * 
     * @param query the {@code BaseQuery} class to be tested
     * @return {@literal true}
     */
    public boolean canConstruct(Class<? extends BaseQuery> query) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    private <T extends BaseQuery> T construct(
            final BaseQueryImpl.ConnectionSource connection,
            final Class<T> query,
            final ClassLoader loader)
            throws InvalidQueryException {
        
        final InvocationHandler handler = new BaseQueryImpl(connection, query);
        return constructProxy(loader, query, handler);
    }

    /**
     * {@inheritDoc}
     */
    public <T extends BaseQuery> T construct(
            final Connection connection,
            final Class<T> query,
            final ClassLoader loader)
            throws InvalidQueryException {
        
        if(TransactionQuery.class.isAssignableFrom(query)) {
            return constructProxy(
                    loader,
                    query,
                    new TransactionQueryImpl(connection, query));
        } else {
            return construct(
                    new BaseQueryImpl.SingleConnectionSource(connection),
                    query,
                    loader);
        }
    }

    /**
     * {@inheritDoc}
     */
    public <T extends BaseQuery> T construct(
            final DataSource dataSource,
            final Class<T> query,
            final ClassLoader loader)
            throws InvalidQueryException {
        
        if(TransactionQuery.class.isAssignableFrom(query)) {
            return constructProxy(
                    loader,
                    query,
                    new TransactionQueryImpl(
                    dataSource,
                    query));
        } else {
            return construct(
                    new BaseQueryImpl.DataSourceConnectionSource(dataSource, true),
                    query,
                    loader);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T constructProxy(
            final ClassLoader loader,
            final Class<T> query,
            final InvocationHandler handler)
            throws IllegalArgumentException {
        
        return (T)Proxy.newProxyInstance(loader, new Class[]{query}, handler);
    }

}
