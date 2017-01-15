package net.lemnik.eodsql.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import java.sql.Savepoint;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.spi.Context;

/**
 * Created on 2008/07/23
 * @author Jason Morris
 */
class TransactionQueryImpl extends BaseQueryImpl {
    final boolean noAutoClose;

    public TransactionQueryImpl(final Connection connection,
            final Class<? extends BaseQuery> clazz) {

        super(new SingleConnectionSource(connection), clazz, TransactionQuery.class);
        this.noAutoClose = false;
        addTransactionMethods();

        try {
            connection.setAutoCommit(false);
        } catch(SQLException ex) {
            throw new EoDException("Cannot set auto-commit on Connection", ex);
        }
    }

    public TransactionQueryImpl(final DataSource datasource,
            final Class<? extends BaseQuery> clazz) {

        super(new DataSourceConnectionSource(datasource, false), clazz, TransactionQuery.class);
        this.noAutoClose = true;
        addTransactionMethods();
    }

    @Override
    protected Context<Annotation> createContext(Annotation annotation, final Object[] args)
    {
        final Context<Annotation> context = new Context<Annotation>(annotation, args);
        if (noAutoClose)
        {
            context.setDontCloseConnection(true);
        }
        return context;
    }
    
    protected void commit() throws SQLException {
	final Connection conn = connectionSource.getConnection();
        conn.commit();
        connectionSource.releaseConnection(conn);
    }

    protected void rollback() throws SQLException {
	final Connection conn = connectionSource.getConnection();
        conn.rollback();
        connectionSource.releaseConnection(conn);
    }

    protected void rollback(Savepoint savepoint) throws SQLException {
	final Connection conn = connectionSource.getConnection();
        conn.rollback(savepoint);
        connectionSource.releaseConnection(conn);
    }

    protected Savepoint setSavepoint() throws SQLException {
	final Connection conn = connectionSource.getConnection();
	try
	{
	    return conn.setSavepoint();
	} finally
	{
	    connectionSource.releaseConnection(conn);
	}
    }

    protected Savepoint setSavepoint(String name) throws SQLException {
	final Connection conn = connectionSource.getConnection();
	try
	{
	    return conn.setSavepoint(name);
	} finally
	{
	    connectionSource.releaseConnection(conn);
	}
    }

    protected void addTransactionMethods() {
        try {
            final InvokeRollback rollback = new InvokeRollback();
            final InvokeSetSavepoint setSavepoint = new InvokeSetSavepoint();

            methods.put(TransactionQuery.class.getMethod("commit"), new InvokeCommit());
            methods.put(TransactionQuery.class.getMethod("close", Boolean.TYPE), new InvokeCommitClose());
            methods.put(TransactionQuery.class.getMethod("rollback"), rollback);
            methods.put(TransactionQuery.class.getMethod("rollback", Savepoint.class), rollback);
            methods.put(TransactionQuery.class.getMethod("setSavepoint"), setSavepoint);
            methods.put(TransactionQuery.class.getMethod("setSavepoint", String.class), setSavepoint);
        } catch(NoSuchMethodException ex) {
        } catch(SecurityException ex) {
        }
    }

    class InvokeCommit implements Callable {
        public Object invoke(final Method method, final Object[] args) throws Throwable {
            commit();
            return null;
        }

    }

    class InvokeCommitClose implements Callable {
        public Object invoke(final Method method, final Object[] args) throws Throwable {
            final Boolean commitFirst = (Boolean)args[0];

            if(commitFirst.equals(Boolean.TRUE)) {
                commit();
            }

            close();
            return null;
        }

    }

    class InvokeRollback implements Callable {
        public Object invoke(final Method method, final Object[] args) throws Throwable {
            if(args == null || args.length == 0) {
                rollback();
            } else {
                rollback((Savepoint)args[0]);
            }

            return null;
        }

    }

    class InvokeSetSavepoint implements Callable {
        public Object invoke(final Method method, final Object[] args) throws Throwable {
            if(args == null || args.length == 0) {
                return setSavepoint();
            } else {
                return setSavepoint((String)args[0]);
            }
        }

    }
}
