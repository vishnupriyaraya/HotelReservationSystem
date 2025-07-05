import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");                 // load driver
            return DriverManager.getConnection("jdbc:sqlite:hotel.db");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("DB Connection Failed: " + e.getMessage());
            return null;
        }
    }
}

