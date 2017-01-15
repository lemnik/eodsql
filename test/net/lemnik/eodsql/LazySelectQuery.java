package net.lemnik.eodsql;

/**
 *
 * @author Jason Morris
 */
public interface LazySelectQuery extends BaseQuery {

    @Update("CREATE TABLE users (" +
    "id BIGINT PRIMARY KEY, " +
    "name VARCHAR(32), " +
    "postCount INTEGER, " +
    "joinDate DATE, " +
    "password VARCHAR(32), " +
    "oldPassword VARCHAR(32), " +
    "email VARCHAR(64)," +
    "bio VARCHAR(256)," +
    "age DATE)")
    void create();

    @Update("DROP TABLE users")
    void drop();

    @Update("INSERT INTO users VALUES (?{1.id}, ?{1.name}, ?{1.postCount}, " +
    "?{1.joinDate}, ?{1.password}, ?{1.oldPassword}, ?{1.email}, ?{1.bio}, " +
    "?{1.age})")
    void insert(final BigUserObject user);

    @Select("SELECT id FROM users WHERE id = ?1")
    BigUserObject selectSkeleton(int id);

    @Select(sql =
    "SELECT name, postCount, joinDate FROM users WHERE id = ?{1.id}", into = 1)
    void selectCoreInfo(BigUserObject user);

    @Select(sql = "SELECT password, oldPassword FROM users WHERE id = ?{1.id}",
    into = 1)
    void selectPasswords(BigUserObject user);

    @Select(sql = "SELECT eamil, bio, age FROM users WHERE id = ?{1.id}",
    into = 1)
    void selectProfileInfo(BigUserObject user);

}
