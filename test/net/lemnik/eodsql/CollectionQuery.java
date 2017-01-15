package net.lemnik.eodsql;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;

/**
 *
 * @author Jason Morris
 */
public interface CollectionQuery extends BaseQuery {

    @Update("CREATE TABLE birthdays (" +
    "birthdate DATE," +
    "who VARCHAR(32))")
    void create();

    @Update("DROP TABLE birthdays")
    void drop();

    @Update("INSERT INTO birthdays VALUES (?1, ?2)")
    void insert(Date when, String who);

    @Update("DELETE FROM birthdays")
    void deleteAll();

    @Select("SELECT * FROM birthdays")
    Set<Birthday> selectSet();

    @Select("SELECT * FROM birthdays")
    List<Birthday> selectList();

    @Select("SELECT * FROM birthdays")
    SortedSet<Birthday> selectSortedSet();

    @Select("SELECT * FROM birthdays")
    Vector<Birthday> selectVector();

}
