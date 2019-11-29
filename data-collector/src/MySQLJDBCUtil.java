import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/*
 * @author Jevgenijus Cistiakovas cistiakj@tcd.ie
 * This is a helpre class to help with easy creation of a JDBC connection to a MySQL isntance.
 */
public class MySQLJDBCUtil {
	/**
	 * Get database connection
	 *
	 * @return a Connection object
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		Connection connection = null;
		try (FileInputStream fs = new FileInputStream("config/dbconfig.properties")) {
			// load the properties file
			Properties dbProps = new Properties();
			dbProps.load(fs);
			// assign db parameters
			String host = dbProps.getProperty("host");
			String db = dbProps.getProperty("db");
			String port = dbProps.getProperty("port");
			String url = String.format("jdbc:mysql://%s:%s/%s", host,port,db);
			String user = dbProps.getProperty("user");
			String password = dbProps.getProperty("password");
			// create a connection to the database
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			connection = DriverManager.getConnection(url, user, password);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		return connection;
	}
}
