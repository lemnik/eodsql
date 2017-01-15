package net.lemnik.eodsql;

/**
 * Created on Sep 16, 2009
 * @author Jason Morris
 */
public class SelectIntoTest extends EoDTestCase {

    private LazySelectQuery query;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        query = QueryTool.getQuery(getConnection(), LazySelectQuery.class);
        query.create();
    }

    @Override
    protected void tearDown() throws Exception {
    	try {
    		query.drop();
    		query = null;
    	} finally {
    		super.tearDown();
    	}
    }

    public void testSelectInto() throws Exception {
        final BigUserObject user1 = new BigUserObject();
        user1.setId(1);
        user1.setName("Klaus");
        user1.setPassword("ppp");
        user1.setOldPassword("ooo");

        query.insert(user1);

        final BigUserObject user2 = new BigUserObject();
        user2.setId(1);
        user2.setOldPassword("overwrite me");
        query.selectPasswords(user2);
        assertEquals("insert into did not retrieve password-property", "ppp", user2.getPassword());
        assertEquals("insert into did not overwrite oldPassword-property", "ooo", user2.getOldPassword());
    }

}
