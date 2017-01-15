EoDSQL is a backwards-compatible implementation of the EoD RI that was to
be included in Java 6. It implements a simple SQL Query / ResultSet -> POJO
mapping layer. It has many advantages over the origional implementation such as

    * A more complex query parser that can inspect method parameters
    * The ability to return simple POJO's, arrays and
      Collection's as well as the DataSet class
    * EoDSQL is JavaBeans friendly, so that you don't
      have to declare fields public
    * DataSet's can be disconnected from the database
      at any time, instead of having to be declared "disconnected"
      in the @Select annotation
    * And many more

EoDSQL is for the most part backwards compatible with the EoD RI from
J2SE, though there are some small differences. For more information
on EoDSQL and how to work with it, look at the JavaDocs in the
"javadoc" directory.
