/*
 * Copyright Jason Morris 2008. All rights reserved.
 */

package net.lemnik.eodsql.mock;

import java.util.Date;

import junit.framework.TestCase;
import net.lemnik.eodsql.DataSet;

import net.lemnik.eodsql.QueryTool;

/**
 *
 * @author Jason Morris
 */
public class MockQueryFactoryTest extends TestCase {
    
    public void testInsert() {
        final UserQuery query = QueryTool.getQuery(UserQuery.class);
        final User id = query.insert(new User("mug@food.com", "Lester Mug", new Date(61, 12, 12)));

        assertNotNull(id);
        assertNotNull(id.getId());
        assertNull(id.getUsername());
        assertNull(id.getEmail());
        assertNull(id.getBirthDate());
    }

    public void testSelectByEmail() {
        final UserQuery query = QueryTool.getQuery(UserQuery.class);
        final User jeff = query.selectByEmail("jeff@jeffswebsite.com");

        assertNotNull(jeff);
        assertEquals(Long.valueOf(1), jeff.getId());
        assertEquals("Jeff Site", jeff.getUsername());
        assertEquals(new Date(76, 8, 23), jeff.getBirthDate());
    }

    public void testSelectDataSet() {
        final UserQuery query = QueryTool.getQuery(UserQuery.class);
        final DataSet<User> users = query.selectUsers();

        assertNotNull(users);
        assertTrue(users.isConnected());
        assertEquals(3, users.size());
    }

}
