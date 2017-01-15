/*
 * Copyright Jason Morris 2008. All rights reserved.
 */

package net.lemnik.eodsql.mock;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import net.lemnik.eodsql.DataSet;

/**
 * <p>
 * </p><p>
 * Created on 13 Apr 2010
 * </p>
 *
 * @author Jason Morris
 */
public class UserQueryMock extends AbstractMockQuery implements UserQuery {

    private final List<User> users = new ArrayList<User>();

    public UserQueryMock() {
        insert(new User("joe.bloggs@nowhere.com", "Joe Bloggs", new Date(83, 3, 6)));
        insert(new User("jeff@jeffswebsite.com", "Jeff Site", new Date(76, 8, 23)));
        insert(new User("logan@murkmurk.com", "Logan Sleep", new Date(90, 4, 1)));
    }

    public User selectByEmail(final String email) {
        for(final User user : users) {
            if(user.getEmail().equals(email)) {
                return user;
            }
        }

        return null;
    }

    public User insert(final User user) {
        final long id = users.size();
        
        final User clonedUser = new User(
                user.getEmail(),
                user.getUsername(),
                user.getBirthDate());
        
        clonedUser.setId(id);

        users.add(clonedUser);

        final User idUser = new User(null, null, null);
        idUser.setId(id);

        return idUser;
    }

    public DataSet<User> selectUsers() {
        return new MockDataSet<User>(users, false, true);
    }

}
