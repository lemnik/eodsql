package net.lemnik.eodsql.spi;

import java.lang.annotation.Annotation;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.lemnik.eodsql.DataSet;

/**
 * <p>
 * Stores the Context information of a single method call. This type is used
 * to store objects such as the {@link java.sql.Connection},
 * the {@link java.sql.Statement} or {@link java.sql.ResultSet}.
 * Each object stored is wrapped in a {@link Resource} object. The
 * {@code Context} object also stores the {@link java.lang.reflect.Method}
 * being invoked and the parameters it has been invoked with.
 * </p><p>
 * By default when a method call is completed, the {@code Context}
 * is {@link #close() closed} causing all of the {@link Resource resources}
 * it holds to be closed and released as well. Sometimes however
 * this is undesired behaviour (for example with a
 * {@link net.lemnik.eodsql.DataIterator}), and the
 * {@link #setAutoclose(boolean)} method can be invoked to change this.
 * </p><p>
 * The {@code Context} object also stores the return-value
 * that will be returned from the invoked method. This value
 * may be changed any number of times during a single method call.
 * </p>
 * 
 * @param <A> the annotation type decorating the method that is being invoked
 * @author Jason Morris
 */
public class Context<A extends Annotation> {

    private static final Object[] EMPTY_PARAMETERS = new Object[0];

    private final Map<Class<?>, Resource<?>> resources =
            new LinkedHashMap<Class<?>, Resource<?>>();

    private final A annotation;

    private final Object[] parameters;

    private Object returnValue = null;

    private boolean autoclose = true;

    private boolean dontCloseConnection = false;

    private boolean closed = false;
    
    private final boolean childContext;

    /**
     * <p>
     * Create a new {@code Context} object with a specified annotation and an array of
     * parameters. The annotation specified should be the one that caused this {@code Context}
     * to be created. The {@code Context} should only live for the duration of a single
     * Method invocation.
     * </p><p>
     * The {@code Context} class elegantly accepts {@literal null} parameter arrays, turning
     * them into empty arrays here. Specifying {@literal null} as the annotation is also legal,
     * but may result in undefined behaviour. The default method implementations have no
     * need of the annotation during their execution however.
     * </p>
     * 
     * @param annotation the annotation containing the methods invocation details
     * @param parameters the parameters passed to the method being invoked
     */
    public Context(final A annotation, final Object[] parameters) {
        this.annotation = annotation;
        this.parameters = parameters != null
                ? parameters
                : EMPTY_PARAMETERS;
        this.childContext = false;
    }

    /**
     * Creates a new {@code Context} object based on a given "parent"
     * {@code Context} with new parameters. This constructor will copy
     * the annotation and all of the current resources from the
     * {@code parentContext} into itself. Resources added to the parent
     * context after this construction will not affect the new {@code Context}.
     * <p>
     * This context is read-only. Calling {@link #setResource(Resource)} will throw an 
     * {@link IllegalStateException}. Calling {@link #close()} on this context is a no-op, i.e.
     * no resources will be freed when calling this. It is the responsibility of the parent context
     * to free the resources. 
     * </p> 
     *
     * @param parentContext the context to copy the state from
     * @param newParameters the new parameters
     * @throws IllegalStateException if the parent {@code Context}
     *      is already closed
     * @throws NullPointerException if the parent {@code Context}
     *      is {@literal null}
     */
    public Context(
            final Context<A> parentContext,
            final Object[] newParameters)
            throws IllegalStateException,
            NullPointerException {

        if(parentContext == null) {
            throw new NullPointerException();
        } else if(parentContext.closed) {
            throw new IllegalStateException("Parent context is already closed");
        }

        this.annotation = parentContext.getAnnotation();
        this.resources.putAll(parentContext.resources);
        this.parameters = newParameters;
        this.autoclose = parentContext.autoclose;
        this.childContext = true;
    }

    @Override
    protected void finalize() throws Exception {
        if (autoclose)
        {
            close();
        }
    }

    /**
     * Returns the annotation that was specified in the constructor. This method may
     * return {@literal null} if no annotation was specified.
     * 
     * @return the annotation that was specified in the constructor
     */
    public A getAnnotation() {
        return annotation;
    }

    /**
     * Returns the parameters that were specified for this method invocation. This
     * method will always return a non-null array, but the array may be empty.
     * 
     * @return the parameters specified for this method invocation
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * Returns the current value that is expected to be returned from this {@code Context}.
     * Sine a {@code Context} represents the internals of a method invocation, this value
     * represents what will be returned from that invocation. The value may optionally change
     * any number of times during the method invocation, and may at any time be {@literal null}.
     * 
     * @return the current value that will be returned when this {@code Context} is closed
     */
    public Object getReturnValue() {
        return returnValue;
    }

    /**
     * Specify a new value to return when this {@code Context} is closed. This method will
     * accept any value and overwrite the current value.
     * 
     * @param returnValue the new value to return from this {@code Context}
     * @see #getReturnValue() 
     */
    public void setReturnValue(final Object returnValue) {
        this.returnValue = returnValue;
    }

    /**
     * <p>
     * Sets a resource in this {@code Context}. To avoid naming problems, resources
     * are mapped based on their types. A {@code Context} cannot hold more than one
     * {@code Resource} of a given {@link Resource#getResourceType() type}. If a
     * {@code Resource} of the specified type already exists in this {@code Context},
     * it will be overwritten by the one specified here.
     * </p><p>
     * Invoking this method with a {@literal null} {@code Resource} object will result
     * in a {@code NullPointerException}.
     * </p>
     * 
     * @param resource the {@code Resource} to place into this {@code Context}
     */
    public void setResource(final Resource<?> resource) {
        if (childContext)
        {
            throw new IllegalStateException("Cannot add resource to nested context");
        }
        resources.put(resource.getResourceType(), resource);
    }

    /**
     * Return a specified {@code Resource} object based on it's {@link Resource#getResourceType()
     * type}. Requesting a {@code Resource} that does has not been set will return {@literal null},
     * while requesting a {@code Resource} that has been closed will thrown an
     * {@code IllegalStateException}.
     * 
     * @param <T> the type of {@code Resource} to fetch from this {@code Context}
     * @param resourceType the {@code Class} descriptor of the {@code Resource} type to fetch
     * @return the {@code Resource} of the specified type that was stored in this
     *      {@code Context}, or {@literal null} if none of the requested type was stored
     * @throws java.lang.IllegalStateException if either this {@code Context} or the requested
     *      {@code Resource} is closed
     */
    public <T> Resource<T> getResource(final Class<T> resourceType)
            throws IllegalStateException {

        if(closed) {
            throw new IllegalStateException(
                    "Cannot fetch a resource from a closed Context object.");
        }
        @SuppressWarnings("unchecked")
        final Resource<T> resource = (Resource<T>)resources.get(resourceType);

        if(resource == null || !resource.isClosed()) {
            return resource;
        } else {
            throw new IllegalStateException(
                    "Resource " +
                    resourceType.getName() +
                    " is already closed in this Context object.");
        }
    }

    /**
     * Sets whether or not to automatically close this {@code Context} when it returns. If the
     * value returned will still need to hold resources in this {@code Context} (for example
     * a connected {@link DataSet}), this should be set to {@literal false}. By default a
     * {@code Context} is in auto-close mode when it is created.
     * 
     * @param autoclose {@literal false} to stop the {@code Context} from being closed when it's
     *      method returns
     */
    public void setAutoclose(final boolean autoclose) {
        this.autoclose = autoclose;
    }

    /**
     * Returns whether or not this {@code Context} object is in auto-close mode.
     * 
     * @return {@literal true} if this {@code Context} will be closed when it's method returns
     * @see #setAutoclose(boolean) 
     */
    public boolean isAutoclose() {
        return autoclose;
    }

    public boolean isDontCloseConnection() {
        return dontCloseConnection;
    }

    public void setDontCloseConnection(boolean closeExceptConnection) {
        this.dontCloseConnection = closeExceptConnection;
    }
    
    private final boolean isConnectionResource(Resource<?> r)
    {
	return r.getResourceType() == Connection.class;
    }

    /**
     * Explicitly close this {@code Context}. This method needs to be invoked to close any
     * remaining {@code Resource}s that this {@code Context} still holds if the {@code Context}
     * is not in {@link #setAutoclose(boolean) auto-close} mode. This method is a no-op if the
     * {@code Context} is already closed.
     * 
     * @throws java.sql.SQLException if one of the {@code Resource}s cannot be closed
     * @see #setAutoclose(boolean) 
     * @see #getResource(java.lang.Class) 
     */
    public void close() throws SQLException {
        if(!closed && !childContext) {
            closed = true;

            // we want to iterate through the values in reverse
            final List<Resource<?>> values = new ArrayList<Resource<?>>(
                    resources.values());
            resources.clear(); // we have a local copy of all the resources now

            // a ListIterator starting at the end
            final ListIterator<Resource<?>> iterator =
                    values.listIterator(values.size());

            while(iterator.hasPrevious()) {
                final Resource<?> r = iterator.previous();
                if (dontCloseConnection && isConnectionResource(r))
                {
                    continue;
                }

                // close each of the Resource objects
                if(!r.isClosed()) {
                    r.close();
                }
            }
        }
    }

}
