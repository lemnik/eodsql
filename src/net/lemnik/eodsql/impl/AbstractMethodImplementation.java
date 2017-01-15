package net.lemnik.eodsql.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.Map;

import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.TypeMapper;
import net.lemnik.eodsql.InvalidQueryException;

import net.lemnik.eodsql.spi.Context;
import net.lemnik.eodsql.spi.StatementResource;
import net.lemnik.eodsql.spi.MethodImplementation;

import net.lemnik.eodsql.spi.util.Query;
import net.lemnik.eodsql.spi.util.ResultSetWrapper;

/**
 * Created on 2008/06/28
 * @author Jason Morris
 */
abstract class AbstractMethodImplementation<A extends Annotation>
        implements MethodImplementation<A> {

    protected ResultSetWrapper<?> wrapper = null;

    protected Query query = null;

    private TypeMapper<?>[] parameterMappers = null;

    protected PreparedStatement createPreparedStatement(
            final Context<A> context)
            throws SQLException {
        
        final Connection connection = context.getResource(Connection.class).get();
        final PreparedStatement statement = connection.prepareStatement(
                query.toString(),
                wrapper.getPreferredResultSetType(),
                wrapper.getPreferredResultSetConcurrency());

        context.setResource(new StatementResource(statement));

        return statement;
    }

    protected void fillPreparedStatementParameters(
            final Context<?> context,
            final PreparedStatement statement)
            throws SQLException {

        final TypeMapper<?>[] mappers = getParameterMappers();

        for(int i = 0; i < mappers.length; i++) {
            final Object parameter = query.getParameter(context, i);
            
            @SuppressWarnings("unchecked")
            final TypeMapper<Object> parameterMapper =
                    (TypeMapper<Object>)mappers[i];

            parameterMapper.set(statement, i + 1, parameter);
        }
    }

    protected TypeMapper<?>[] getParameterMappers() {
        TypeMapper[] mappers = parameterMappers;
        
        if(mappers == null) {
            mappers = getParameterTypeMappers(query, null);
            parameterMappers = mappers;
        }

        return mappers;
    }

    protected void setParameterMappers(final Class<? extends TypeMapper>[] customTypeMapperClasses) {
        parameterMappers = getParameterTypeMappers(query, customTypeMapperClasses);
    }
    
    protected static TypeMapper<?>[] getParameterTypeMappers(final Query query,
            final Class<? extends TypeMapper>[] customTypeMapperClasses) {
        final int parameters = query.getParameterCount();
        final TypeMapper<?>[] mappers = new TypeMapper<?>[parameters];
        final Map<Class, TypeMapper> knownMappers = QueryTool.getTypeMap();

        for(int i = 0; i < parameters; i++) {
            final Constructor<? extends TypeMapper> customMapperConstructor 
            	= getConstructor(customTypeMapperClasses, i);
            if (customMapperConstructor != null) {
        	try {
        	    mappers[i] = customMapperConstructor.newInstance();
        	} catch (Exception ex) {
        	    throw new InvalidQueryException("Cannot construct custom type mapper " 
        		    + customMapperConstructor.getName(), ex);
        	}
            } else {
            final TypeMapper mapper = knownMappers.get(query.getParameterType(i));

            if(mapper != null) {
                mappers[i] = mapper;
            } else {
        	    throw new InvalidQueryException("Unknown primitive type: " +
                        query.getParameterType(i).getName());
            }
        }
        }

        return mappers;
    }

    private static Constructor<? extends TypeMapper> getConstructor(
            Class<? extends TypeMapper>[] customTypeMapperClasses, int i) {
        if (customTypeMapperClasses == null || i >= customTypeMapperClasses.length) {
            return null;
        }
        final Class<? extends TypeMapper> customTypeMapperClass = customTypeMapperClasses[i];
        if (customTypeMapperClass.isInterface()) {
            return null;
        }
        try {
            return customTypeMapperClass.getConstructor();
        } catch (Exception ex) {
            throw new IllegalArgumentException("TypeMapper classes must have a default " +
                    "(null) constructor", ex);
        }
    }

}
