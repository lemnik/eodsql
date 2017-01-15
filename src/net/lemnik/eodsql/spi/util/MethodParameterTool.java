package net.lemnik.eodsql.spi.util;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author jason
 */
interface MethodParameterTool {

    public Object getParameter(Object... params);

    public Class getParameterType();

    // <editor-fold defaultstate="collapsed" desc=" Default ">
    class Parameter implements MethodParameterTool {

        private final int paramIndex;

        private final Class type;

        /** Creates a new instance of DefaultParameterHandler */
        public Parameter(final int paramIndex, final Class type) {
            this.paramIndex = paramIndex;
            this.type = type;
        }

        public Object getParameter(final Object... params) {
            return params[paramIndex];
        }

        public Class getParameterType() {
            return type;
        }

    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Field ">
    class Field implements MethodParameterTool {

        private final int paramIndex;

        private final java.lang.reflect.Field field;

        /** Creates a new instance of FieldParameterHandler */
        public Field(
                final int paramIndex,
                final java.lang.reflect.Field field) {

            this.paramIndex = paramIndex;
            this.field = field;
        }

        public Object getParameter(final Object... params) {
            try {
                return field.get(params[paramIndex]);
            } catch(final IllegalArgumentException argumentException) {
                throw new RuntimeException(argumentException);
            } catch(final IllegalAccessException accessException) {
                throw new RuntimeException(accessException);
            }
        }

        public Class getParameterType() {
            return field.getType();
        }

    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Method ">
    class Method implements MethodParameterTool {

        private final int paramIndex;

        private final java.lang.reflect.Method method;

        /** Creates a new instance of MethodParameterHandler */
        public Method(
                final int paramIndex,
                final java.lang.reflect.Method method) {

            this.paramIndex = paramIndex;
            this.method = method;
        }

        public Object getParameter(final Object... params) {
            try {
                return method.invoke(params[paramIndex]);
            } catch(final IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            } catch(final IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch(final InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

        public Class getParameterType() {
            return method.getReturnType();
        }

    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc=" Chained ">
    class Chained implements MethodParameterTool {

        private final MethodParameterTool from;

        private final MethodParameterTool to;

        /** Creates a new instance of ChainedParameterHandler */
        public Chained(
                final MethodParameterTool from,
                final MethodParameterTool to) {

            this.from = from;
            this.to = to;
        }

        public Object getParameter(final Object... params) {
            return to.getParameter(from.getParameter(params));
        }

        public Class getParameterType() {
            return to.getParameterType();
        }

    }
    // </editor-fold>
}
