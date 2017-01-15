package net.lemnik.eodsql;

import java.util.Date;

/**
 * Created on Sep 16, 2009
 * @author Jason Morris
 */
public class Birthday implements Comparable<Birthday> {

    private Date birthdate;

    private String who;

    private Birthday() {
    }

    public Birthday(final Date when, final String who) {
        this.birthdate = when;
        this.who = who;
    }

    public Date getWhen() {
        return birthdate;
    }

    public String getWho() {
        return who;
    }

    public int compareTo(final Birthday o) {
        return birthdate.compareTo(o.birthdate);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof Birthday) {
            final Birthday b = (Birthday)obj;
            return getWhen().equals(b.getWhen()) &&
                    getWho().equals(b.getWho());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getWhen().hashCode() ^ getWho().hashCode();
    }

    @Override
    public String toString() {
        return getWho() + " - " + getWhen();
    }

}
