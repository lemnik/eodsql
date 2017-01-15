package net.lemnik.eodsql.spi;

import java.lang.annotation.Annotation;

/**
 * <p>
 * This class represents the implementation of a method. The actual
 * details of how the method is to be executed are hidden behind
 * the {@link #invoke(Context) invoke} method.
 * </p><p>
 * Implementations of this interface will generally be meta-implementations
 * of methods, where their exact behaviour is defined by an annotation, however
 * complete concrete implementations are allowed as long as they are coupled
 * with a marker annotation and {@link MethodImplementationFactory}.
 * </p>
 *
 * @param <A> the annotation type that signals the use of this
 *      {@code MethodImplementation}
 * @author Jason Morris
 */
public interface MethodImplementation<A extends Annotation> {

    /**
     * Invoke this {@code MethodImplementation} within the specified
     * {@code Context}. This method may throw any {@code Throwable},
     * which may be wrapped in a {@code RuntimeException} if it is not
     * declared on the method signature.
     *
     * @param context the context the method should execute within
     * @throws Throwable if something goes wrong during execution
     */
    void invoke(Context<A> context) throws Throwable;

}
