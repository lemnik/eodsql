/*
 * Copyright Jason Morris 2008. All rights reserved.
 */
package net.lemnik.eodsql;

/**
 *
 * @author Jason Morris
 */
public interface LooseMappingQuery extends BaseQuery {
    @Update("INSERT INTO rows VALUES (?{1.col1}, ?{1.col2}, ?{1.col3}, ?{1.col4})")
    public void insert(CompleteRow row);

    @Select("SELECT * FROM rows")
    public SubsetRow[] select();

    @Select("SELECT * FROM rows")
    public DataSet<SubsetRow> selectDataSet();

    @Select("SELECT * FROM rows")
    public DataIterator<SubsetRow> selectDataIterator();

    @Update("CREATE TABLE rows (col1 VARCHAR(256), col2 VARCHAR(256), col3 VARCHAR(256), col4 VARCHAR(256))")
    public void create();

    @Update("DROP TABLE rows")
    public void drop();

    public static class CompleteRow {
        public int col1;

        public int col2;

        public int col3;

        public int col4;

    }

    public static class SubsetRow {
        public int col2;

        public int col3;

        public String nonExistantColumn = null;

        public SubsetRow() {
        }

        public SubsetRow(int col2, int col3) {
            this.col2 = col2;
            this.col3 = col3;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) {
                return false;
            }
            if(getClass() != obj.getClass()) {
                return false;
            }
            final SubsetRow other = (SubsetRow)obj;
            if(this.col2 != other.col2) {
                return false;
            }
            if(this.col3 != other.col3) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + this.col2;
            hash = 67 * hash + this.col3;
            return hash;
        }
        @Override
        public String toString() {
            return "SubsetRow[col2=" + col2 + ",col3=" + col3 + "]";
        }

    }
}
