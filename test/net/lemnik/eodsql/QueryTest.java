/*
 * Copyright Jason Morris 2008. All rights reserved.
 */
package net.lemnik.eodsql;

import java.lang.reflect.Method;

import java.text.ParseException;

import junit.framework.TestCase;

import net.lemnik.eodsql.spi.util.Query;

/**
 * Created on Apr 28, 2009
 * @author Jason Morris
 */
public class QueryTest extends TestCase {

    private void myTestMethod(KeyObject object) {
        // here for Query.validate
    }

    public void testQueryCache() throws ParseException {
        final String query = "SELECT * FROM users";

        final Query query1 = Query.getQuery(query);
        final Query query2 = Query.getQuery(query);
        final Query query3 = Query.getQuery(query);
        final Query query4 = Query.getQuery("SELECT * FROM not_users");

        assertSame(query1, query2);
        assertSame(query2, query3);
        assertNotSame(query1, query4);
    }

    public void testQueryStringNoParameterIndex() throws Exception {
        final Query query1 = Query.getQuery(
                "SELECT * FROM users WHERE id = ?{id}",
                KeyObject.class);

        assertSame(Long.class, query1.getParameterType(0));

        final Method method = getClass().getDeclaredMethod("myTestMethod", KeyObject.class);
        Query.validate("INSERT INTO keys (id, value) VALUES(?{id}, ?{value})", method);
    }

    public void testInvalidQueryString() {
        try {
            new Query("SELECT * FROM users WHERE id = ?");
            fail("Query string was invalid, there should have been an exception");
        } catch(final ParseException pe) {
            // pass!
        }

        try {
            new Query("SELECT * FROM users WHERE id = ?abcdefg");
            fail("Query string was invalid, there should have been an exception");
        } catch(final ParseException pe) {
            // pass!
        }
    }

}
