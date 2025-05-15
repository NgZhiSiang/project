package team.project;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver"; 
    private static final String DB_URL_BASE = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "burger"; 
    private static final String DB_URL_PARAMS = "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    private static final String DB_URL = DB_URL_BASE + DB_NAME + DB_URL_PARAMS;

    private static final String DB_USERNAME = "root"; 
    private static final String DB_PASSWORD = "Grimm589"; 

    
    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
           
        }
    }


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }


    public static void main(String[] args) {
        Connection connection = null;
        try {
            System.out.println("Attempting to connect to database: " + DB_NAME);
            System.out.println("Connection URL: " + DB_URL);
            System.out.println("Username: " + DB_USERNAME);

            connection = getConnection();

            if (connection != null && !connection.isClosed()) {
                System.out.println("Successfully connected to the database!");
                System.out.println("Database Product Name: " + connection.getMetaData().getDatabaseProductName());
                System.out.println("Database Product Version: " + connection.getMetaData().getDatabaseProductVersion());
            } else {
                System.err.println("Failed to make connection to the database.");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace(); 
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Database connection closed.");
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}