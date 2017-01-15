package net.lemnik.eodsql.spi;

import java.lang.reflect.Method;

import java.lang.annotation.Annotation;

import net.lemnik.eodsql.InvalidQueryException;

/**
 * This interface can be used to provide third-party
 * {@code MethodImplementation} objects.
 *
 * @param <A> the annotation type that will decorate methods to be implemented
 *      by this {@code MethodImplementationFactory}
 * @author Jason Morris
 */
public interface MethodImplementationFactory<A extends Annotation> {

    void validate(Method method) throws InvalidQueryException;

    MethodImplementation<A> createImplementation(Method method);

}
