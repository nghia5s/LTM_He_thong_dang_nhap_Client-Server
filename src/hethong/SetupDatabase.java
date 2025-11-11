package hethong;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class SetupDatabase {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                // 🏗️ Tạo bảng nếu chưa có
                String sql = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL,
                        email TEXT,
                        status TEXT DEFAULT 'Hoạt động',
                        loginStatus TEXT DEFAULT 'Offline'
                    );
                """;
                stmt.execute(sql);

                System.out.println("✅ Bảng users đã được tạo (nếu chưa có).");

                // 🔍 Kiểm tra xem tài khoản admin đã tồn tại chưa
                String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
                ResultSet rs = stmt.executeQuery(checkAdmin);
                rs.next();
                int count = rs.getInt(1);
                rs.close();

                // 👑 Nếu chưa có admin -> thêm mới
                if (count == 0) {
                    String insertAdmin = """
                        INSERT INTO users (username, password, role, email, status, loginStatus)
                        VALUES (?, ?, ?, ?, ?, ?)
                    """;
                    try (PreparedStatement pstmt = conn.prepareStatement(insertAdmin)) {
                        pstmt.setString(1, "admin");
                        pstmt.setString(2, "admin123"); // 🔐 mật khẩu mặc định
                        pstmt.setString(3, "ADMIN");
                        pstmt.setString(4, "admin@example.com");
                        pstmt.setString(5, "Hoạt động");
                        pstmt.setString(6, "Offline");
                        pstmt.executeUpdate();
                        System.out.println("✅ Tài khoản admin mặc định đã được tạo.");
                    }
                } else {
                    System.out.println("ℹ️ Tài khoản admin đã tồn tại, bỏ qua.");
                }

                System.out.println("🎯 Cấu hình cơ sở dữ liệu hoàn tất!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
