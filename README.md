# eodsql
EoDSQL -- "Ease of Development", in case you were wondering -- is a lightweight
object-relational bridge for Java. It relies only on the standard JDBC APIs,
and uses only a small number of annotations to specify data mappings between Java and relational databases.
This means, of course, that it can only be used with Java 1.5 or newer.
EoDSQL uses no external configuration files or XML.

# Getting Starting

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

Declare a Query interface next, this if often done inside your data-object class.

```
public interface PersonDAI extends BaseQuery {
    @Select("SELECT * FROM people WHERE id = ?1")
    PersonDO find(long id);
}
```

Initialize the `PersonDAI` somewhere. Typically you'll use a global connection pool via the `QueryTool.setDefaultDataSource(DataSource)`, in which
case you can declare the instance statically; often within the data-object class:

```
public class PersonDO {
    public static final PersonDAI QUERY = QueryTool.getQuery(PersonDAI.class);

    // ...
}
```