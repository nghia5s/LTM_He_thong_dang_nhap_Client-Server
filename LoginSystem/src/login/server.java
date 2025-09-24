package login;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.io.File;

public class server {
    private static final int PORT = 12345;
    private static final String DB_URL = "jdbc:sqlite:users.db";

    public static void main(String[] args) {
        // In ra đường dẫn tuyệt đối của file DB
        File dbFile = new File("users.db");
        System.out.println("📂 File database sẽ được tạo tại: " + dbFile.getAbsolutePath());

        initDatabase();
        createDefaultAdmin();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Server đang chạy trên cổng " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("🔗 Kết nối mới từ: " + socket.getInetAddress());
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Kết nối database
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // 🔹 Tạo bảng nếu chưa có
    private static void initDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "fullname TEXT," +
                "dob TEXT," +   // 🔹 đổi từ age sang dob
                "phone TEXT," +
                "email TEXT)";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Bảng users đã sẵn sàng.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Tạo admin mặc định
    private static void createDefaultAdmin() {
        String checkSql = "SELECT * FROM users WHERE username='admin'";
        String insertSql = "INSERT INTO users(username,password,role,fullname,dob,phone,email) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(checkSql);
            if (!rs.next()) {
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin123");
                    pstmt.setString(3, "admin");
                    pstmt.setString(4, "Administrator");
                    pstmt.setString(5, "2000-01-01"); // 🔹 ngày sinh mặc định
                    pstmt.setString(6, "0000000000");
                    pstmt.setString(7, "admin@example.com");
                    pstmt.executeUpdate();
                    System.out.println("✅ Đã tạo tài khoản admin mặc định: admin / admin123");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Validate dữ liệu
    private static boolean isValidFullname(String fullname) {
        return fullname.matches("([A-ZÀ-Ỹ][a-zà-ỹ]*)( [A-ZÀ-Ỹ][a-zà-ỹ]*)*") && fullname.length() <= 30;
    }

    private static boolean isValidPhone(String phone) {
        return phone.matches("\\d{1,10}");
    }

    private static boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9._%+-]+@gmail\\.com$");
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String request = in.readLine();
                System.out.println("📩 Yêu cầu từ client: " + request);

                if (request != null) {
                    String[] parts = request.split("\\|");
                    String action = parts[0];

                    switch (action.toUpperCase()) {
                        case "LOGIN": {
                            String username = parts[1];
                            String password = parts[2];
                            String userRole = checkLogin(username, password);
                            if (userRole != null) {
                                if ("admin".equalsIgnoreCase(userRole)) {
                                    out.println("SUCCESS:ADMIN");
                                    sendUserList(out);
                                } else {
                                    out.println("SUCCESS");
                                }
                            } else {
                                out.println("FAIL");
                            }
                            break;
                        }
                        case "REGISTER": {
                            if (parts.length < 8) {
                                out.println("FAIL");
                                break;
                            }
                            String username = parts[1];
                            String password = parts[2];
                            String role = parts[3];
                            String fullname = parts[4];
                            String dob = parts[5];   // 🔹 đổi từ age sang dob
                            String phone = parts[6];
                            String email = parts[7];

                            if (!isValidFullname(fullname) || !isValidPhone(phone) || !isValidEmail(email)) {
                                out.println("FAIL:INVALID_DATA");
                                break;
                            }

                            out.println(registerUser(username, password, role, fullname, dob, phone, email) ? "SUCCESS" : "FAIL");
                            break;
                        }
                        case "DELETE": {
                            String usernameToDelete = parts[1];
                            out.println(deleteUser(usernameToDelete) ? "DELETE_SUCCESS" : "DELETE_FAIL");
                            break;
                        }
                        case "UPDATE": {
                            if (parts.length < 9) {
                                out.println("UPDATE_FAIL");
                                break;
                            }
                            String oldUsername = parts[1];
                            String newUsername = parts[2];
                            String newPassword = parts[3];
                            String newRole = parts[4];
                            String newFullname = parts[5];
                            String newDob = parts[6];   // 🔹 đổi từ age sang dob
                            String newPhone = parts[7];
                            String newEmail = parts[8];
                            out.println(updateUser(oldUsername, newUsername, newPassword, newRole, newFullname, newDob, newPhone, newEmail) 
                                        ? "UPDATE_SUCCESS" : "UPDATE_FAIL");
                            break;
                        }
                        case "GET_ALL_USERS": {
                            sendUserList(out);
                            break;
                        }

                        default:
                            out.println("UNKNOWN_COMMAND");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String checkLogin(String username, String password) {
            String sql = "SELECT role FROM users WHERE username=? AND password=?";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("role");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void sendUserList(PrintWriter out) {
            String sql = "SELECT username,password,role,fullname,dob,phone,email FROM users";
            try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                out.println("USERLIST_START");
                while (rs.next()) {
                    out.println(
                            rs.getString("username") + "," +
                            rs.getString("password") + "," +
                            rs.getString("role") + "," +
                            rs.getString("fullname") + "," +
                            rs.getString("dob") + "," +
                            rs.getString("phone") + "," +
                            rs.getString("email")
                    );
                }
                out.println("END");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private boolean registerUser(String username, String password, String role,
                                     String fullname, String dob, String phone, String email) {
            String sql = "INSERT INTO users(username,password,role,fullname,dob,phone,email) VALUES(?,?,?,?,?,?,?)";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, role);
                pstmt.setString(4, fullname);
                pstmt.setString(5, dob);
                pstmt.setString(6, phone);
                pstmt.setString(7, email);
                pstmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.out.println("❌ Lỗi đăng ký: " + e.getMessage());
                return false;
            }
        }

        private boolean deleteUser(String usernameToDelete) {
            if ("admin".equalsIgnoreCase(usernameToDelete)) return false;
            String sql = "DELETE FROM users WHERE username=?";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, usernameToDelete);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean updateUser(String oldUsername, String newUsername, String newPassword,
                                   String newRole, String newFullname, String newDob,
                                   String newPhone, String newEmail) {
            String sql = "UPDATE users SET username=?, password=?, role=?, fullname=?, dob=?, phone=?, email=? WHERE username=?";
            try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newUsername);
                pstmt.setString(2, newPassword);
                pstmt.setString(3, newRole);
                pstmt.setString(4, newFullname);
                pstmt.setString(5, newDob);
                pstmt.setString(6, newPhone);
                pstmt.setString(7, newEmail);
                pstmt.setString(8, oldUsername);
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
