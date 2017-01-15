package net.lemnik.eodsql;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 *    By default columns for <code>ResultSet</code> are mapped directly to public
 *    fields within the data object class. The <code>ResultColumn</code> annotation
 *    allows you to change the way the column is mapped.
 * </p><p>
 *    <pre>
 *        public class User {
 *            public String username;
 *            
 *            <span style="color: #00f;">@ResultColumn("birth_date")</span>
 *            public Date birthDate;
 *        }
 *    </pre>
 *    
 *    Instead of looking for a column named <code>"birthDate"</code> in the 
 *    <code>ResultSet</code>, the mapping code will now look for a column
 *    named <code>"birth_date"</code> and map it to the <code>"birthDate"</code>
 *    variable.
 * </p><p>
 *     <code>@ResultColumn</code> can also map columns to setter methods. The
 *     method does not have to follow any naming convention, but accept only
 *     a single, simple parameter type (which is the type the column is expected
 *     to be).
 *     
 *     <pre>
 *         public class User {
 *            private String name;
 *            
 *            ...
 *            
 *            <span style="color: #00f;">@ResultColumn("username")</span>
 *            public void setUsername(String name) {
 *                this.name = name;
 *            }
 *         }
 *     </pre>
 *     
 *     The above code will attempt to map a column named <tt>"username"</tt> to
 *     the data object, by invoking the <tt>"setUsername"</tt> method. This
 *     mapping totally ignored the name of the method, and it's parameters.
 *     Note that if a method annotated by <code>@ResultColumn</code> takes more
 *     than one parameter, an {@link java.lang.IllegalArgumentException} will
 *     be thrown during validation by the {@link QueryTool}.
 *</p><p>
 *     <b>Note:</b> All column names in EoD SQL are treated as case
 *     in-sensitive, since different JDBC drivers respond with different
 *     meta-data.
 *</p>
 *            
 * @author jason
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultColumn {

    /**
     * The name of the field as it will appear in the
     * <code>ResultSet</code> meta-data. If no
     * <code>ResultColumn</code> is found annotating a public field,
     * it is assumed that the field will be mapped on it's variable-name.
     */
    String value();

}