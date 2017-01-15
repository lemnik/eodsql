package net.lemnik.eodsql.spi.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.text.ParseException;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.InvalidQueryException;

import net.lemnik.eodsql.spi.Context;

/**
 * This class represents an immutable EoD-SQL query. An EoD-SQL query allows for the parameter
 * description to be built into the SQL. This class parses the query and produces a plain
 * SQL string (with prepared '?' placeholders for injected parameters), and then allows for
 * extraction of the parameters from a {@link Context} object.
 * 
 * @author Jason Morris
 */
public final class Query {

    private static final MethodParameterTool[] EMPTY_PARAMETERS =
            new MethodParameterTool[0];

    private final String sql;

    private MethodParameterTool[] parameters = EMPTY_PARAMETERS;

    /**
     * <p>
     * Create a new {@code Query} object. The given query string will be
     * parsed, and errors will be reported. This constructor is to be
     * considered "heavy weight", since it does some fairly detailed
     * inspection of the parameters in order to validate the query string.
     * </p><p>
     * For a lighter weight alternative use the
     * {@link #getQuery(String, Class[])} method which makes use of a
     * cache of {@code Query} objects ({@code Query} objects are immutable and
     * therefore thread-safe). If {@code Query} objects are created on a
     * per connection or per-request they can quickly become very
     * expensive.
     * </p>
     *
     * @param eodsql the query string to be parsed
     * @param parameterTypes the types of the parameters that will be
     *      in the {@code Context} in the {@link #getParameter(Context, int)}
     *      method
     * @throws java.text.ParseException if the query string cannot be
     *      parsed or the parameter types cannot supply the values required
     *      by the query string
     * @see #getParameter(Context, int)
     * @see Context#getParameters() 
     */
    public Query(
            final String eodsql,
            final Class<?>... parameterTypes)
            throws ParseException {

        sql = parseQueryString(eodsql, parameterTypes);
    }

    /**
     * Extracts an indexed parameter from the specified {@link Context} object. The index
     * is where the parameter was specified in the query-string, not within the {@link Context}.
     * 
     * @param context the {@code Context} to extract the parameter from
     * @param index the index in the query string of the requested parameter (starting at {@literal 0}).
     * @return the parameter value
     */
    public Object getParameter(final Context context, final int index) {
        return parameters[index].getParameter(context.getParameters());
    }

    /**
     * Reurns an {@link Iterator} that can be used to fetch parameters from the specified
     * {@link Context}. Note that the {@link Iterator#remove() remove} method will throw an
     * {@link UnsupportedOperationException}.
     * 
     * @param context the Context to extract parameters from
     * @return an {@link Iterator} to extract parameter objects from the context
     */
    public Iterator<Object> getParameterIterator(final Context context) {
        return new ParameterIterator(context);
    }

    /**
     * Returns the type of parameter that will be required for a specific index within
     * this {@code Query}.
     * 
     * @param index the index of the parameter to look for. The first index is {@literal 0}.
     * @return the class type of the parameter for the given index
     */
    public Class<?> getParameterType(final int index) {
        return parameters[index].getParameterType();
    }

    /**
     * Returns the number of parameters that this query will need. This has no relation to the
     * number of parameters passed to a method. A method may take one parameter, which is
     * introspected by the query into any number of parameters. It could be the other way around
     * as well.
     * 
     * @return the number of parameters that can be fetched using
     *      {@link #getParameter(net.lemnik.eodsql.spi.Context, int)}
     */
    public int getParameterCount() {
        return parameters.length;
    }

    /**
     * Returns the SQL representation of this {@code Query}. This could be used with a
     * {@code PreparedStatement} without modification.
     * 
     * @return the SQL that was extracted for this {@code Query}
     */
    @Override
    public String toString() {
        return sql;
    }
    // <editor-fold defaultstate="collapsed" desc=" validation implementation ">

    /**
     * <p>
     * A utility method to validate an EoD SQL query string. This method does nothing
     * to validate the SQL, but does attempt to make sure all of the EoD SQL parameters
     * are well formed and have matching parameters in the given method. It will also
     * look to make sure that the properties or fields referenced in the query are
     * visible.
     * </p><p>
     * The advantage of this method is that it's a lot cheaper than allocating a new
     * {@code Query} object.
     * </p>
     * 
     * @param query the query string to validate
     * @param method the method with which the query is asociated
     */
    public static void validate(final String query, final Method method) {
        int index = 0;

        while(index < query.length()) {
            char ch = query.charAt(index);

            if(ch == '?') {
                ch = getCharacter(query, index++, method);

                if(Character.isDigit(ch)) {
                    index = validateSimpleQueryParameter(query, index, method);
                    continue;
                } else {
                    index = validateComplexQueryParameter(query, index, method);
                    continue;
                }
            }

            index++;
        }
    }

    private static int validateSimpleQueryParameter(
            final String query,
            final int startIndex,
            final Method method) {

        final StringBuilder builder = new StringBuilder();

        int index = startIndex;

        while(index < query.length()) {
            char ch = query.charAt(index);
            if(Character.isDigit(ch)) {
                builder.append(ch);
            } else {
                break;
            }

            index++;
        }

        final int paramIndex = Integer.parseInt(builder.toString()) - 1;
        validateQueryParameter(paramIndex, method);

        return index;
    }

    private static int validateComplexQueryParameter(
            final String query,
            final int index,
            final Method method) {

        final int end = query.indexOf('}', index + 1);

        if(end == -1) {
            throw new InvalidQueryException(
                    "Unclosed complex parameter at " + index, method);
        } else {
            final String param = query.substring(index + 1, end).trim();
            final String[] parts = param.split("\\.");

            // do a quick check to make sure none of the parts have spaces
            for(final String p : parts) {
                for(int i = 0; i < p.length(); i++) {
                    if(Character.isWhitespace(p.charAt(i))) {
                        throw new InvalidQueryException(
                                "Not a valid atom " + p, method);
                    }
                }
            }
        }

        return end + 1;
    }

    private static void validateQueryParameter(
            final int paramIndex,
            final Method method) {

        final Class[] types = method.getParameterTypes();

        if(paramIndex < 0 || paramIndex >= types.length) {
            throw new InvalidQueryException("Invalid parameter index for" +
                    " method " + paramIndex + 1, method);
        } else {
            if(!QueryTool.getTypeMap().containsKey(types[paramIndex])) {
                throw new InvalidQueryException(
                        types[paramIndex].getName() +
                        " is not a known primitive type and cannot be " +
                        "specified as a query parameter.", method);
            }
        }
    }

    private static char getCharacter(
            final String string,
            final int index,
            final Method method) {

        if(index < string.length()) {
            return string.charAt(index);
        } else {
            throw new InvalidQueryException(
                    "Expected a character, but " +
                    "hit the end of the string: '" + string + "'.", method);
        }
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" parsing code ">

    private String parseQueryString(
            final String sql,
            final Class[] parameterTypes)
            throws ParseException {

        final StringBuilder query = new StringBuilder();
        final QueryReader reader = new QueryReader(sql);

        final List<MethodParameterTool> list =
                new ArrayList<MethodParameterTool>(0);

        try {
            int ch = 0;

            while((ch = reader.read()) != -1) {
                switch(ch) {
                    case '?':
                        query.append('?');
                        list.add(createParameterHandler(reader, parameterTypes));
                        break;
                    default:
                        query.append((char)ch);
                }
            }
        } catch(final NullPointerException ex) {
            throw new ParseException(
                    "cannot parse query string!",
                    reader.getIndex());
        }

        if(!list.isEmpty()) {
            parameters = list.toArray(new MethodParameterTool[list.size()]);
        }

        return query.toString();
    }

    private MethodParameterTool createParameterHandler(
            final QueryReader reader,
            final Class[] parameterTypes) throws ParseException {

        final int ch = reader.read();

        if(ch == -1) {
            throw new ParseException(
                    "trailing ? character in query string",
                    reader.getIndex());
        } else if(Character.isDigit(ch)) {
            return createSimpleParameterHandler(reader, parameterTypes, ch);
        } else if(ch != '{' && !reader.skipUntil('{')) {
            throw new ParseException(
                    "cannot find complex query opening (for ?{param} syntax)",
                    reader.getIndex());
        }

        final String param = reader.readUntil('}').trim();
        final String[] parts = param.split("\\.");

        MethodParameterTool handler = null;

        // if the first part starts with a digit, it's a parameter index
        if(Character.isDigit(parts[0].charAt(0))) {
            final int paramIndex = Integer.parseInt(parts[0]) - 1;
            final Class type = parameterTypes[paramIndex];

            if(parts.length == 1) {
                handler = new MethodParameterTool.Parameter(paramIndex, type);
            } else if(parts.length == 2) {
                handler = getParameterHandler(paramIndex, parts[1], type);
            } else {
                handler = getParameterHandler(paramIndex, parts[1], type);

                for(int i = 2; i < parts.length; i++) {
                    final MethodParameterTool next =
                            getParameterHandler(
                            0,
                            parts[i],
                            handler.getParameterType());

                    handler = new MethodParameterTool.Chained(handler, next);
                }
            }
        } else {
            // if we don't have a start parameter index, we assume it's 0
            final Class type = parameterTypes[0];
            handler = getParameterHandler(0, parts[0], type);

            if(parts.length > 1) {
                for(int i = 1; i < parts.length; i++) {
                    final MethodParameterTool next =
                            getParameterHandler(
                            0,
                            parts[i],
                            handler.getParameterType());

                    handler = new MethodParameterTool.Chained(handler, next);
                }
            }
        }

        return handler;
    }

    private MethodParameterTool getParameterHandler(
            final int parameterIndex,
            final String name,
            final Class sourceType) {

        try {
            final Field field = sourceType.getField(name);

            if((Modifier.isPublic(field.getModifiers()) ||
                    DataObjectBindingCache.HAVE_ACCESSIBLE_PERMISSION) &&
                    !Modifier.isStatic(field.getModifiers())) {

                return new MethodParameterTool.Field(parameterIndex, field);
            }
        } catch(final SecurityException ex) {
            // ignore...
        } catch(final NoSuchFieldException ex) {
            // then we continue our search
        }

        final Method readMethod = findReadMethod(name, sourceType);
        if(readMethod != null) {
            return new MethodParameterTool.Method(parameterIndex, readMethod);
        }

        throw new IllegalArgumentException("Cannot find field / property " +
                sourceType.getName() + "." + name);
    }

    private Method findReadMethod(final String name, final Class type) {
        try {
            // Introspector caches the BeanInfo internally
            final BeanInfo info = Introspector.getBeanInfo(type);
            final PropertyDescriptor[] desc = info.getPropertyDescriptors();

            // This isn't the fastes way to deal with this
            for(final PropertyDescriptor tmp : desc) {
                if(tmp.getName().equals(name)) {
                    return tmp.getReadMethod();
                }
            }
        } catch(final IntrospectionException ex) {
            // we just return null at the end
        }

        return null;
    }

    private MethodParameterTool createSimpleParameterHandler(
            final QueryReader reader,
            final Class[] parameterTypes,
            final int ch1) throws ParseException {

        final int startIndex = reader.getIndex();
        final StringBuilder sb = new StringBuilder(2);
        sb.append((char)ch1);

        int ch = -1;
        boolean done = false;

        do {
            ch = reader.read();

            if(Character.isDigit(ch)) {
                sb.append((char)ch);
            } else {
                done = true;
            }
        } while(!done);

        if(ch != -1) {
            reader.skip(-1);
        }

        final int index = Integer.parseInt(sb.toString()) - 1;

        if(index < 0 || index >= parameterTypes.length) {
            throw new ParseException(
                    "parameter index out of bounds: " + (index + 1),
                    startIndex);
        }

        return new MethodParameterTool.Parameter(
                index,
                parameterTypes[index]);
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" query caching code ">

    private static final QueryCache CACHE = new QueryCache();

    /**
     * <p>
     * This is the preferred method for fetching a {@code Query} object. This
     * method makes internal use of a {@link SoftReference} cache in order to
     * avoid re-parsing commonly used queries. Any {@code Query} fetched from
     * this method will be cached in memory and re-used if possible
     * (and required).
     * </p><p>
     * The parameters to this method are identical to the
     * {@link Query#Query(String, Class[]) constructor}.
     * </p>
     *
     * @param eodsql the query string to be parsed
     * @param parameterTypes the types of the parameters that will be
     *      in the {@code Context} in the {@link #getParameter(Context, int)}
     *      method
     * @return the {@code Query} object for the given query string and
     *      parameter types
     * @throws java.text.ParseException if the query string cannot be
     *      parsed or the parameter types cannot supply the values required
     *      by the query string
     * @since 2.1
     */
    public static Query getQuery(
            final String eodsql,
            final Class<?>... parameterTypes)
            throws ParseException {

        return CACHE.getQuery(eodsql, parameterTypes);
    }

    private static class QueryKey {

        private final String sql;

        private final Class<?>[] parameterTypes;

        public QueryKey(final String sql, final Class<?>[] parameterTypes) {
            this.sql = sql;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public int hashCode() {
            // mostly queries are unique if their SQL is that same
            // rather than pollute the hash-code with the parameter-types
            // we rather leave that to equals
            return sql.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if(obj instanceof QueryKey) {
                final QueryKey other = (QueryKey)obj;
                return sql.equals(other.sql) &&
                        Arrays.equals(parameterTypes, other.parameterTypes);
            }

            return false;
        }

    }

    private static class CachedQuery extends SoftReference<Query> {

        private final QueryKey key;

        public CachedQuery(
                final QueryKey key,
                final Query query,
                final ReferenceQueue<Query> queue) {

            super(query, queue);
            this.key = key;
        }

        public QueryKey getQueryKey() {
            return key;
        }

    }

    private static class QueryCache {

        private final ConcurrentMap<QueryKey, CachedQuery> cache;

        private final ReferenceQueue<Query> referenceQueue;

        public QueryCache() {
            referenceQueue = new ReferenceQueue<Query>();
            cache = new ConcurrentHashMap<QueryKey, CachedQuery>();
        }

        private void purgeStaleQueries() {
            CachedQuery query = null;

            while((query = (CachedQuery)referenceQueue.poll()) != null) {
                cache.remove(query.getQueryKey(), query);
            }
        }

        public Query getQuery(final String sql, final Class<?>[] paramTypes)
                throws ParseException {

            purgeStaleQueries();

            final QueryKey key = new QueryKey(sql, paramTypes);
            final CachedQuery cachedQuery = cache.get(key);

            Query query = cachedQuery != null
                    ? cachedQuery.get()
                    : null;

            if(query == null) {
                if(cachedQuery != null) {
                    cache.remove(key, cachedQuery);
                }

                query = new Query(sql, paramTypes);

                final CachedQuery newCachedQuery =
                        new CachedQuery(key, query, referenceQueue);

                if(cache.putIfAbsent(key, newCachedQuery) != null) {
                    // cycle round and try-again
                    return getQuery(sql, paramTypes);
                }
            }

            return query;
        }

    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" counting reader implementation ">

    private static class QueryReader {

        private final String source;

        private int index;

        private int length;

        private int mark = 0;

        public QueryReader(String source) {
            this.source = source;
            this.length = source.length();
            this.index = 0;
        }

        public boolean skipUntil(final char c) {
            do {
                mark();
                int ch = read();

                if(ch == -1) {
                    reset();
                    return false;
                } else if(ch == c) {
                    return true;
                }
            } while(true);
        }

        public String readUntil(final char c) {
            StringBuilder builder = new StringBuilder();

            do {
                int ch = read();

                if(ch == -1) {
                    return null;
                } else if(ch == c) {
                    return builder.toString();
                } else {
                    builder.append((char)ch);
                }
            } while(true);
        }

        public int read() {
            if(index < length) {
                return source.charAt(index++);
            } else {
                return -1;
            }
        }

        public long skip(final int n) {
            index += n;

            if(index < 0) {
                index = 0;
            } else if(index > length) {
                index = length;
            }

            return n;
        }

        public void mark() {
            mark = index;
        }

        public void reset() {
            index = mark;
        }

        public int getIndex() {
            return index;
        }

    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" parameter iterator implementation ">

    private class ParameterIterator implements Iterator<Object> {

        private final Object[] inputParameters;

        private int index = 0;

        private ParameterIterator(Context context) {
            this.inputParameters = context.getParameters();
        }

        public boolean hasNext() {
            return index < parameters.length;
        }

        public Object next() {
            return parameters[index++].getParameter(inputParameters);
        }

        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

    }    // </editor-fold>
}
