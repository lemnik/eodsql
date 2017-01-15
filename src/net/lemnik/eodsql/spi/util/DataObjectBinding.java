package net.lemnik.eodsql.spi.util;

import java.lang.reflect.Type;
import java.lang.reflect.Modifier;
import java.lang.reflect.Constructor;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.InvocationTargetException;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Arrays;
import java.util.Map;
import java.util.IdentityHashMap;

import net.lemnik.eodsql.QueryTool;
import net.lemnik.eodsql.EoDException;
import net.lemnik.eodsql.InvalidDataTypeException;

/**
 * Represents the binding of a single row in a {@code ResultSet} to an object.
 * This class may be implemented by hand to allow for faster binding
 * between {@code ResultSet}'s and DataObjects. Default implementation exist
 * for objects conforming to the EoD SQL "data-object" structure, and
 * for any object that has a registered {@link net.lemnik.eodsql.TypeMapper}.
 *
 * @param <T> the type to bind {@code ResultSet} data to
 * @author Jason Morris
 */
public abstract class DataObjectBinding<T> {

    private static final Map<Class<?>, Class<? extends DataObjectBinding<?>>>
            KNOWN_BINDINGS = new IdentityHashMap<
            Class<?>,
            Class<? extends DataObjectBinding<?>>>();

    private static Constructor<? extends DataObjectBinding>
            DEFAULT_BINDING_CONSTRUCTOR = null;

    static {
        setDataObjectBinding(MapDataObjectBinding.getStringObjectMapObjectType(), MapDataObjectBinding.class);
    }
    
    /**
     * An empty array of String's that can be used when there are no key columns
     * known for the data-object type. This should only be returned when this
     * {@code DataObjectBinding} is not a {@link BindingType#KEYS_BINDING}.
     */
    public static final String[] NO_KEY_COLUMNS = new String[0];

    private BindingType bindingType = BindingType.NORMAL_BINDING;

    private Class<T> objectType = null;

    /**
     * Create a new, empty {@code DataObjectBinding}.
     */
    protected DataObjectBinding() {
    }

    @SuppressWarnings("unchecked")
    private Class<T> getObjectTypeImpl(final TypeVariable[] parameters) {
        for(final TypeVariable v : parameters) {
            if(v.getName().equals("T")) {
                final Type bound = v.getBounds()[0];

                if(bound instanceof ParameterizedType &&
                        ((ParameterizedType)bound).getRawType() instanceof Class) {

                    return (Class)((ParameterizedType)bound).getRawType();
                } else if(bound instanceof Class) {
                    return (Class)bound;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> DataObjectBinding<T> createDefaultDataObjectBinding(
            final Class<T> dataObject,
            final BindingType bindingType) {

        if(DEFAULT_BINDING_CONSTRUCTOR != null) {
            try {
                final DataObjectBinding<T> binding =
                        DEFAULT_BINDING_CONSTRUCTOR.newInstance(dataObject);
                return trySetBindingType(binding, bindingType);
            } catch(final InstantiationException ie) {
                throw new EoDException(ie);
            } catch(final IllegalAccessException iae) {
                throw new EoDException(iae);
            } catch(final IllegalArgumentException iae) {
                throw new EoDException(iae);
            } catch(final InvocationTargetException ite) {
                throw new EoDException(ite);
            }
        } else if(QueryTool.getTypeMap().containsKey(dataObject)) {
            return new TypeMapperDataObjectBinding<T>(
                    QueryTool.getTypeMap().get(dataObject),
                    dataObject);
        } else {
            DefaultDataObjectBinding.validate(dataObject);

            final DataObjectBinding<T> binding =
                    new DefaultDataObjectBinding<T>(dataObject);
            return trySetBindingType(binding, bindingType);
        }
    }

    private static <T> DataObjectBinding<T> trySetBindingType(
            final DataObjectBinding<T> binding,
            final BindingType bindingType) {

        if(!binding.setBindingType(bindingType)) {
            return null;
        } else {
            return binding;
        }
    }

    /**
     * By default, this method will reflect on the current class and try to figure out it's
     * type passed on the generic {@code T} declaration. If {@code T} is a wildcard with an
     * upper-boundry, or a simple {@code Class} this method will return a valid {@code Class}
     * object. Otherwise this method will throw an {@code IllegalArgumentException}.
     * 
     * @return the {@code Class} type that will be returned by this {@code DataObjectBinding}
     */
    public Class<T> getObjectType() {
        if(objectType == null) {
            final Class clazz = getClass();
            final TypeVariable[] parameters = clazz.getTypeParameters();

            if(parameters == null || parameters.length == 0) {
                throw new IllegalArgumentException(
                        "No type parameters specified on class " +
                        clazz.getName());
            }

            objectType = getObjectTypeImpl(parameters);
        }

        return objectType;
    }

    /**
     * Sets the type of object this {@code DataObjectBinding} will return from it's
     * {@link #unmarshall(java.sql.ResultSet)} methods. This method should be invoked
     * in the constructor of the {@code DataObjectBinding} so that {@link #getObjectType()}
     * will return the expected value.
     * 
     * @param objectType the object type that this {@code DataObjectBinding} will return from
     *      it's {@link #unmarshall(java.sql.ResultSet)} methods
     * @see #getObjectType() 
     */
    protected void setObjectType(final Class<T> objectType) {
        this.objectType = objectType;
    }

    /**
     * Map the current row in the specified {@code ResultSet} to an object. This method will
     * call the implementations of {@link #unmarshall(java.sql.ResultSet, java.lang.Object)} and
     * {@link #newInstance()} to create the data-object and unmarshall the row data into it.
     * 
     * @param row the {@code ResultSet} containing the data
     * @return a new object containing the data that was found in the {@code ResultSet}
     * @see #unmarshall(java.sql.ResultSet, java.lang.Object)
     * @throws java.sql.SQLException if a database error occured
     * @throws net.lemnik.eodsql.EoDException if a mapping error occured
     */
    public T unmarshall(final ResultSet row)
            throws SQLException, EoDException {

        T object = newInstance();
        unmarshall(row, object);

        return object;
    }

    /**
     * Returns the type of binding this object represents.
     *
     * @return the current type of binding done by
     * this {@code DataObjectBinding}
     */
    public BindingType getBindingType() {
        return bindingType;
    }

    /**
     * Sets the type of binding this {@code DataObjectBinding} will be performing.
     * This method can be overridden to "listen" for changes, and reject them if required.
     * 
     * @param bindingType the new {@code BindingType} of this {@code DataObjectBinding}
     * @return {@literal true} if the {@code BindingType} was changed, {@literal false} if the
     *      request was rejected.
     */
    public boolean setBindingType(final BindingType bindingType) {
        this.bindingType = bindingType;
        return true;
    }

    /**
     * Returned whether or not this {@code DataObjectBinding} can be used for rubberstamping.
     * The general contract is that if the returned data-object is entirely mutable, then
     * it is capable of rubberstamping. If the data-object is immutable, rubberstamping cannot
     * be done (since a new object needs to be constructed for each row).
     * 
     * @return {@literal true} by default.
     */
    public boolean isRubberstampCapable() {
        return true;
    }

    /**
     * <p>
     * Test to see if this {@code DataObjectBinding} object is capable of pushing an object
     * into a {@code ResultSet} with the {@link #marshall(java.lang.Object, java.sql.ResultSet)}
     * method. If this method returns {@literal false} then it can be assumed that any call to
     * {@link #marshall(java.lang.Object, java.sql.ResultSet) marshal} will result in an
     * {@code UnsupportedOperationException}.
     * </p><p>
     * By default this method returns {@literal true}, and so should be overridden by
     * implementors that cannot update a {@code ResultSet}.
     * </p>
     * 
     * @return {@literal true} if invoking the
     *      {@link #marshall(java.lang.Object, java.sql.ResultSet) marshal} will not result in
     *      an {@code UnsupportedOperationException}
     */
    public boolean isUpdateCapable() {
        return true;
    }

    /**
     * <p>
     * Create a new, empty instance of the data-object this object will
     * bind to. This method may return {@literal null} if the data-object
     * is an immutable primitive type.
     * </p><p>
     * By default this method delegates to the {@link Class#newInstance()}
     * method of this {@code DataObjectBinding}'s {@link #getObjectType()
     * object-type}. Any reflection exceptions are wrapped in an
     * {@code EoDException} and rethrown with a suitable error message.
     * </p>
     * 
     * @return a new instance of the data-object
     * @throws EoDException if the data-obejct could not be created
     */
    public T newInstance() throws EoDException {
        try {
            return getObjectType().newInstance();
        } catch(final InstantiationException ie) {
            throw new EoDException("Cannot instantiate type: " +
                    getObjectType().getName() +
                    ". Make sure it has a public default constrcutor.",
                    ie);
        } catch(final IllegalAccessException iae) {
            throw new EoDException("Cannot instantiate type: " +
                    getObjectType().getName() +
                    ". Make sure it has a public default constrcutor.",
                    iae);
        }
    }

    /**
     * If this is a {@link BindingType#KEYS_BINDING}, this method should
     * return a list of the database column names that should be
     * requested from the database.
     * 
     * @return by default {@link #NO_KEY_COLUMNS}
     */
    public String[] getKeyColumnNames() {
        return NO_KEY_COLUMNS;
    }

    @Override
    public String toString() {
        switch(bindingType) {
            case FIRST_COLUMN_BINDING:
            case KEYS_BINDING:
                return getClass().getName() + ":" +
                        objectType.getName() +
                        Arrays.toString(getKeyColumnNames());
                default:
                    return getClass().getName() + ":" +
                        objectType.getName();
        }
    }

    /**
     * <p>
     * Unmarshall a {@code ResultSet} into a specified object. This method is used for
     * any {@link #isRubberstampCapable() rubberstamping} wrapper object. If the wrapper
     * object is not rubberstamping, this method will not be invoked. Non-rubberstamping
     * {@link ResultSetWrapper wrappers} should make use of the
     * {@link #unmarshall(java.sql.ResultSet)} method instead of this one.
     * </p><p>
     * This method should make no effort to navigate the given {@code ResultSet}. The cursor
     * of the {@code ResultSet} will be at the row to be unmarshalled and will be nagivated by
     * the {@link ResultSetWrapper wrapper} object if needed.
     * </p><p>
     * If rubberstamping is not supported by this {@code DataObjectBinding}, this method
     * may throw an {@code UnsupportedOperationException}.
     * </p>
     * 
     * @param row the {@code ResultSet} to unmarshall from
     * @param into the mutable object to unmarshall into
     * @throws java.sql.SQLException if a database error occures
     * @throws net.lemnik.eodsql.EoDException if an unmarshalling error occures
     * @throws java.lang.UnsupportedOperationException if rubberstamping is not supported by
     *      this {@code DataObjectBinding} object
     */
    public abstract void unmarshall(
            final ResultSet row,
            final T into)
            throws SQLException,
            EoDException;

    /**
     * Marshall a specified object into a {@code ResultSet}. This method should only be used
     * if a call to {@link #isUpdateCapable()} returns {@literal true}. This method will
     * attempt to place the fields of the given object into the specified {@code ResultSet}.
     * The {@code ResultSet} given here should be in the same format as the one given to
     * {@link #unmarshall(java.sql.ResultSet)}, failure to do so can result in an
     * {@code EoDException}.
     * 
     * @param from the object to marshall
     * @param results the {@code ResultSet} who's columns are to be set
     * @throws java.sql.SQLException if an database error occures
     * @throws net.lemnik.eodsql.EoDException if a marshalling error occures
     */
    public abstract void marshall(
            final T from,
            final ResultSet results)
            throws SQLException,
            EoDException;

    /**
     * Sets the {@code DataObjectBinding} class for a specified Data-Object type. A
     * {@code DataObjectBinding} class must have a public default (null) constructor.
     * 
     * @param <T> the data-type that the {@code DataObjectBinding} will bind to
     * @param dataObject the data-type that the {@code DataObjectBinding} will bind to
     * @param bindingClass the {@code DataObjectBinding} class
     * @throws java.lang.IllegalArgumentException if the {@code DataObjectBinding} class does
     *      not have a public default constructor
     */
    public static <T> void setDataObjectBinding(
            final Class<T> dataObject,
            final Class<? extends DataObjectBinding<T>> bindingClass)
            throws IllegalArgumentException {

        try {
            final Constructor constructor = bindingClass.getConstructor();

            if(!Modifier.isPublic(constructor.getModifiers())) {
                throw new IllegalArgumentException(
                        "DataObjectBinding classes must have a " +
                        "default (null) constructor");
            }
        } catch(final NoSuchMethodException methodException) {
            throw new IllegalArgumentException(
                    "DataObjectBinding classes must have a default " +
                    "(null) constructor", methodException);
        } catch(final SecurityException securityException) {
            throw new IllegalArgumentException(
                    "DataObjectBinding classes must have a default " +
                    "(null) constructor", securityException);
        }

        synchronized(DataObjectBinding.class) {
            KNOWN_BINDINGS.put(dataObject, bindingClass);
        }
    }

    /**
     * Try to create a {@code DataObjectBinding} for the specified {@code dataObject} class.
     * This method will look at any known {@code DataObjectBinding}'s, and if none can be found,
     * it will fall back on the {@link #setDefaultDataObjectBinding(Class) default}
     * {@code DataObjectBinding}.
     * 
     * @param <T> the data-type object
     * @param dataObject the data-type object to be bound
     * @param bindingType the type of binding the the returned {@code DataObjectBinding} should
     *      perform
     * @return a {@code DataObjectBinding} to perform the specified type of binding between
     *      {@code ResultSet}s and the given {@code dataObject}
     * @throws EoDException if a {@code DataObjectBinding} cannot be created
     *      for the given data object and binding type
     */
    public static <T> DataObjectBinding<T> getDataObjectBinding(
            final Class<T> dataObject,
            final BindingType bindingType)
            throws EoDException {

        final Class<? extends DataObjectBinding<?>> bindingClass =
                KNOWN_BINDINGS.get(dataObject);

        if(bindingClass == null) {
            return createDefaultDataObjectBinding(dataObject, bindingType);
        } else {
            try {
                @SuppressWarnings(value = "unchecked")
                final DataObjectBinding<T> binding =
                        (DataObjectBinding<T>)bindingClass.newInstance();
                if (binding.getObjectType() == null) {
                    binding.setObjectType(dataObject);
                }
                return trySetBindingType(binding, bindingType);
            } catch(final InstantiationException instantiationException) {
                throw new EoDException(instantiationException);
            } catch(final IllegalAccessException accessException) {
                throw new EoDException(accessException);
            }
        }
    }

    /**
     * <p>
     * Set the Default-DataObjectBinding type. The type specified here must have a
     * constructor with exactly the following signature (other than the DataObjectBinding class
     * name):
     * </p><p>
     * <pre>public DataObjectBinding(Class&lt;?&gt;);</pre>
     * </p><p>
     * If no {@code DataObjectBinding} has been set for a class that is requested through
     * the {@link #getDataObjectBinding(java.lang.Class,
     * net.lemnik.eodsql.spi.util.DataObjectBinding.BindingType)} method, an instance of
     * the given type will be created passing the parameters of the {@code getDataObjectBinding}
     * method into the constructor.
     * </p>
     * 
     * @param defaultBinding the binding type to create for unknown DataObject types
     * @throws java.lang.IllegalArgumentException if the {@code DataObjectBinding} doesn't have
     *      the required constructor
     */
    public static void setDefaultDataObjectBinding(
            final Class<? extends DataObjectBinding> defaultBinding)
            throws IllegalArgumentException {

        try {
            final Constructor<? extends DataObjectBinding> constructor =
                    defaultBinding.getConstructor(Class.class);

            if(!Modifier.isPublic(constructor.getModifiers())) {
                throw new IllegalArgumentException("The constructor of the " +
                        "Default-DataObjectBinding class must be public.");
            }

            DEFAULT_BINDING_CONSTRUCTOR = constructor;
        } catch(final NoSuchMethodException noSuchMethodException) {
            throw new IllegalArgumentException(
                    "The Default-DataObjectBinding class must have " +
                    "a public Constructor(Class<?>)", noSuchMethodException);
        }
    }

    /**
     * Validate a given data-object class against known {@code DataObjectBinding}'s.
     * 
     * @param dataObjectType the data-object class to validate
     * @throws net.lemnik.eodsql.InvalidDataTypeException if the data-object cannot be bound.
     */
    public static void validate(
            final Class<?> dataObjectType)
            throws InvalidDataTypeException {

        if(!KNOWN_BINDINGS.containsKey(dataObjectType)) {
            if(DEFAULT_BINDING_CONSTRUCTOR != null) {
                try {
                    DEFAULT_BINDING_CONSTRUCTOR.newInstance(dataObjectType);
                } catch(final InstantiationException ie) {
                    throw new InvalidDataTypeException(
                            "Cannot create Default-DataObjectBinding " +
                            "for type.", dataObjectType);
                } catch(final IllegalAccessException iae) {
                    throw new InvalidDataTypeException(
                            "Cannot create Default-DataObjectBinding " +
                            "for type.", dataObjectType);
                } catch(final IllegalArgumentException iae) {
                    throw new InvalidDataTypeException(
                            "Cannot create Default-DataObjectBinding " +
                            "for type.", dataObjectType);
                } catch(final InvocationTargetException ite) {
                    if(ite.getTargetException() instanceof InvalidDataTypeException) {
                        throw (InvalidDataTypeException)ite.getTargetException();
                    } else {
                        throw new InvalidDataTypeException(
                                "Cannot create Default-DataObjectBinding " +
                                "for type.", dataObjectType);
                    }
                }
            } else if(!QueryTool.getTypeMap().containsKey(dataObjectType)) {
                DefaultDataObjectBinding.validate(dataObjectType);
            }
        }
    }

    /**
     * Used to specify the type of binding that should be performed between a {@code ResultSet}
     * and a data-object.
     */
    public static enum BindingType {

        /**
         * A normal binding, any fields or properties that can be bound to a column in the
         * {@code ResultSet} are bound. If there are additional fields, properties or columns
         * they are ignored.
         */
        NORMAL_BINDING,
        /**
         * A Keys binding. This means that only the fields and properties marked as database
         * generated keys should be bound to columns in the {@code ResultSet}. If the entire
         * data-object is marked as a key, then all of it's bindable fields and properties
         * should be bound.
         */
        KEYS_BINDING,
        /**
         * A special type of Keys binding. The data-type should only have one field or
         * property marked as a key. This field or property will be bound to the first
         * column of the {@code ResultSet}, no matter what the name of the column.
         */
        FIRST_COLUMN_BINDING

    }
}
