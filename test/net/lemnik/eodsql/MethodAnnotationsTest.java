/*
 * Copyright Jason Morris 2008. All rights reserved.
 */
package net.lemnik.eodsql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Created on 2008/10/05
 * @author Jason Morris
 */
public class MethodAnnotationsTest extends EoDTestCase {
    
    public void testSingleAnnotationQuery() throws Exception {
        QueryTool.getQuery(getConnection(), SingleAnnotationQuery.class).close();
    }
    
    public void testMultipleAnnotationQuery() throws Exception {
        QueryTool.getQuery(getConnection(), MultipleAnnotationQuery.class).close();
    }
    
    public void testInvalidQuery() throws Exception {
        try {
            QueryTool.getQuery(getConnection(), InvalidQuery.class).close();
            fail("InvalidQuery was successfully created and closed.");
        } catch(InvalidQueryException iqe) {
            // pass... this should happen
        }
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public static @interface TestAnnotation {
        
    }
    
    public static interface SingleAnnotationQuery extends BaseQuery {
        @Select("SELECT string_column FROM null_table")
        public List<String> selectMethod();
    }
    
    public static interface MultipleAnnotationQuery extends BaseQuery {
        @Select("SELECT string_column FROM null_table")
        @TestAnnotation
        public List<String> selectMethod();
    }
    
    public static interface InvalidQuery extends BaseQuery {
        @Select("SELECT string_column FROM null_table")
        @Update("UPDATE null_table SET string_column = 'foo'")
        public List<String> invalidMethod();
    }
}
