/*
 * EoDTestCase.java
 *
 * Created on September 13, 2007, 2:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.lemnik.eodsql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import junit.framework.TestCase;

/**
 *
 * @author jason
 */
public abstract class EoDTestCase extends TestCase {

    private static String driverName;

    private static String url;

    private static String user;

    private static String password;

    private File dir;

    private Connection connection;

    private BasicDataSource dataSource;

    static {
        driverName = System.getProperty("test.db.driver");
        url = System.getProperty("test.db.url");
        user = System.getProperty("test.db.user");
        password = System.getProperty("test.db.password");

        if(driverName == null) {
            final Properties props = loadBuildProperties();
            driverName = props.getProperty("test.db.driver");
            url = props.getProperty("test.db.url");
            user = props.getProperty("test.db.user");
            password = props.getProperty("test.db.password");
        }
    }

    protected EoDTestCase() {
    }

    protected EoDTestCase(String testName) {
        super(testName);
    }

    private void delete(File dir) {
        if(dir != null && dir.exists()) {
            for(File f : dir.listFiles()) {
                if(f.isFile()) {
                    f.delete();
                } else {
                    delete(f);
                }
            }

            dir.delete();
        }
    }

    private static Properties loadBuildProperties() {
        File propFile = new File("build.properties");
        if(propFile.exists()) {
            return loadBuildProperties(propFile);
        } else {
            propFile = new File("eodsql/build.properties");
            if(propFile.exists()) {
                return loadBuildProperties(propFile);
            } else {
                return null;
            }
        }
    }

    private static Properties loadBuildProperties(File propFile) {
        final Properties result = new Properties();
        FileInputStream is = null;
        try {
            is = new FileInputStream(propFile);
            result.load(is);
            is.close();
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private void loadConnection(Connection connection) throws IOException, SQLException {
        InputStream in = getClass().getResourceAsStream(getClass().getSimpleName() + ".sql");

        if(in != null) {
            Statement statement = connection.createStatement();
            InputStreamReader isr = new InputStreamReader(in);
            StringBuilder builder = new StringBuilder();

            int ch = -1;

            while((ch = isr.read()) != -1) {
                if(ch == ';') {
                    statement.execute(builder.toString());
                    builder.setLength(0);
                } else {
                    builder.appendCodePoint(ch);
                }
            }

            isr.close();
            in.close();
            statement.close();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        if(!connection.isClosed()) {
            if(!connection.getAutoCommit()) {
                connection.rollback();
                connection.commit();
            }

            connection.close();
            connection = null;
        }

        delete(dir);
    }

    protected Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        if(connection == null || connection.isClosed()) {
            Class.forName(driverName);

            connection = DriverManager.getConnection(url, user, password);
            loadConnection(connection);
        }

        return connection;
    }

    protected DataSource getDataSource(boolean autoCommit) throws ClassNotFoundException {
        if(dataSource == null) {
            dataSource = new BasicDataSource();
            Class.forName(driverName);
            dataSource.setDriverClassName(driverName);

            dataSource.setUrl(url);
            dataSource.setUsername(user);
            dataSource.setPassword(password);
        }
        dataSource.setDefaultAutoCommit(autoCommit);
        return dataSource;
    }
}
