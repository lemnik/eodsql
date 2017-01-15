# eodsql
EoDSQL -- "Ease of Development", in case you were wondering -- is a lightweight
object-relational bridge for Java. It relies only on the standard JDBC APIs,
and uses only a small number of annotations to specify data mappings between Java and relational databases.
This means, of course, that it can only be used with Java 1.5 or newer.
EoDSQL uses no external configuration files or XML.

# Getting Starting

Download the latest (2.2) release from here: https://github.com/lemnik/eodsql/releases/tag/2.2

Declare a model class that represents your database table:
```
public class PersonDO {
    public int id;
    public String name;
    public String loginID;
    public String email;
    public Date birthDate;
}
```

* `public` fields are optional, getters and setters are also acceptable (and may be `private` if you like)
* A default constructor is required (javac will insert one in the above code), but may also be `private` if you only want EoDSQL to get to it

Declare a Query interface next, this if often done inside your data-object class:

```
public class PersonDO {
    // ...

    public interface PersonDAI extends BaseQuery {
        @Select("SELECT * FROM people WHERE id = ?1")
        PersonDO find(long id);
        
        @Select("SELECT * FROM people ORDER BY birthDate")
        DataSet<PersonDO> all();
        
        @Select("SELECT * FROM people ORDER BY birthDate")
        PersonDO[] allEager();
        
        @Update("UPDATE people SET "
            + "name = ?{1.name}, "
            + "loginID = ?{1.loginID}, "
            + "email = ?{1.email}, "
            + "birthDate = ?{1.birthDate} "
            + "WHERE id = ?{1.id}")
        void update(PersonDO person);
    }
}
```

* If you declare `throws SQLException` on your query methods, they won't wrap your errors in `RuntimeException`
* You can declare that a `@Select` method returns any implementation of `List`, `Set` or even an array if you like
* Use `@ResultColumn` annotations on fields or methods to handle differences between Java names and SQL names

Initialize the `PersonDAI` somewhere. Typically you'll use a global connection pool via the `QueryTool.setDefaultDataSource(DataSource)`, in which
case you can declare the instance statically; often within the data-object class:

```
public class PersonDO {
    public static final PersonDAI QUERY = QueryTool.getQuery(PersonDAI.class);

    // ...
}
```

# Some more tricks

It's common to want to hide whats really going on within your data-object classes,
and as such EoDSQL will quick readily make use of `private` methods, fields and constructors
when you tell it to. Using this technique it's easy to keep that database-specific
logic away from things calling your data classes.