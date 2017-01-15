/*
 * SimpleObject.java
 *
 * Created on March 15, 2007, 1:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.lemnik.eodsql;

import java.util.UUID;

/**
 *
 * @author jason
 */
public class SimpleObject {

    public UUID id;

    public String data;

    @ResultColumn("index")
    public int order;

    /** Creates a new instance of SimpleObject */
    public SimpleObject() {
    }

    @Override
    public String toString() {
        return "SimpleObject[id=" + id + ", data=" + data + ", order=" + order + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final SimpleObject other = (SimpleObject)obj;
        if(this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if((this.data == null)
                ? (other.data != null)
                : !this.data.equals(other.data)) {
            return false;
        }
        if(this.order != other.order) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + (this.id != null
                ? this.id.hashCode()
                : 0);
        hash = 47 * hash + (this.data != null
                ? this.data.hashCode()
                : 0);
        hash = 47 * hash + this.order;
        return hash;
    }

}
