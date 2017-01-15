package net.lemnik.eodsql;

/**
 *
 * @author Jason Morris
 */
public interface AccessibilityTestQuery extends BaseQuery {
    @Update("CREATE TABLE objects (id BIGINT, name VARCHAR(36))")
    public void create();

    @Update("DROP TABLE objects")
    public void drop();

    @Update("INSERT INTO objects VALUES (?{1.id}, ?{1.name})")
    public void insert(InaccessibleObject object);

    @Select("SELECT * FROM objects")
    public InaccessibleObject[] select();

    public static class InaccessibleObject {
        private long id;

        private String name;

        InaccessibleObject() {
        }

        public InaccessibleObject(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == null) {
                return false;
            }
            if(getClass() != obj.getClass()) {
                return false;
            }
            final InaccessibleObject other = (InaccessibleObject)obj;
            if(this.id != other.id) {
                return false;
            }
            if(this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + (int)(this.id ^ (this.id >>> 32));
            hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "InaccessibleObject[id=" + id + ",name=" + name + "]";
        }

    }
}
