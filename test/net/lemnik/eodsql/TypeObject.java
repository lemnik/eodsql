/*
 * TypeObject.java
 *
 * Created on March 13, 2007, 3:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.lemnik.eodsql;

import java.util.Date;
import java.util.UUID;

/**
 *
 * @author jason
 */
public class TypeObject {
    @ResultColumn("byte_col")
    public Byte byteObject;
    
    @ResultColumn("short_col")
    public Short shortObject;
    
    @ResultColumn("int_col")
    public Integer intObject;
    
    @ResultColumn("long_col")
    public Long longObject;
    
    @ResultColumn("float_col")
    public Float floatObject;
    
    @ResultColumn("double_col")
    public Double doubleObject;
    
    @ResultColumn("bool_col")
    public Boolean booleanObject;
    
    @ResultColumn("string_col")
    public String stringObject;
    
    @ResultColumn("date_col")
    public Date dateObject;
    
    @ResultColumn("uuid_col")
    public UUID uuidObject;
    
    /**
     * Creates a new instance of TypeObject
     */
    public TypeObject() {
    }
    
}
