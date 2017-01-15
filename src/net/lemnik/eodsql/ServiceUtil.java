package net.lemnik.eodsql;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

/**
 * This is an abstraction of the {@code ServiceLoader} class that exists in
 * Java 1.6. In order to keep EoD SQL running under Java 1.5 this class will
 * test to see if the {@code ServiceLoader} class is available, and if
 * not return empty {@code Iterable} objects.
 *
 * @author Jason Morris
 * @since 2.1
 */
class ServiceUtil {

    /**
     * We are a very simple utility class that is only really used during
     * the startup of the system, so we have no fields and no public
     * constructor.
     */
    private ServiceUtil() {
    }

    /**
     * This is the replacement of the {@link ServiceLoader#load(Class)}
     * method. It will first attempt to invoke the {@code ServiceLoader.load}
     * method with reflection, if this fails this method will return
     * an empty {@code Iterable} object.
     *
     * @param <T> the type we are trying to load with a {@code ServiceLoader}
     * @param type the type class we are trying to load
     *      with a {@code ServiceLoader}
     * @return an {@code Iterable} containing registered instances of the
     *      specified {@code type} if the {@code ServiceLoader} class is
     *      available, otherwise an empty {@code Iterable} object
     */
    @SuppressWarnings("unchecked")
    static <T> Iterable<T> load(final Class<T> type) {
        try {
            return (Iterable<T>)Class.forName(
                    "java.util.ServiceLoader").
                    getMethod("load", Class.class).
                    invoke(null, type);
        } catch(final NoClassDefFoundError e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(final IllegalArgumentException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(final SecurityException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(final IllegalAccessException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(final InvocationTargetException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(final NoSuchMethodException e) {
            // ignore this... we are simply not running 1.6 or higher
        } catch(final ClassNotFoundException e) {
            // ignore this... we are simply not running 1.6 or higher
        }

        return Collections.emptySet();
    }

}
