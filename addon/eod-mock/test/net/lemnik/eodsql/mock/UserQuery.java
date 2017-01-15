/*
 * Copyright Jason Morris 2008. All rights reserved.
 */
package net.lemnik.eodsql.mock;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.GeneratedKeys;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

/**
 * <p>
 * </p><p>
 * Created on 13 Apr 2010
 * </p>
 *
 * @author Jason Morris
 */
public interface UserQuery extends BaseQuery {

    @Select("SELECT * FROM users WHERE email = ?1")
    User selectByEmail(String email);

    @Update(sql = "INSERT INTO users (email, username, birth_date) "
    + "VALUES(?{1.email}, ?{1.username}, ?{1.birthDate})",
    keys = GeneratedKeys.RETURNED_KEYS_FIRST_COLUMN)
    User insert(User user);

    @Select("SELECT * FROM users")
    DataSet<User> selectUsers();

}
