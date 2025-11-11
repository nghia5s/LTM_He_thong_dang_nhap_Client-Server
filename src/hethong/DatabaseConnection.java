package hethong;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:users.db"; // tên file db

    public static Connection getConnection() {
        try {
            String path = new java.io.File("users.db").getAbsolutePath();
            System.out.println("🔍 Database path: " + path); // In ra đường dẫn thật sự
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
