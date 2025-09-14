package login;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class server {
    private static final int PORT = 12345;
    private static final String USER_FILE = "users.txt";

    public static void main(String[] args) {
        createDefaultAdmin();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server đang chạy trên cổng " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Kết nối mới: " + socket.getInetAddress());
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ Tạo tài khoản admin mặc định
    private static void createDefaultAdmin() {
        File file = new File(USER_FILE);
        try {
            if (!file.exists()) file.createNewFile();
            boolean hasAdmin = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3 && parts[2].equals("admin")) {
                        hasAdmin = true;
                        break;
                    }
                }
            }

            if (!hasAdmin) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                    writer.write("admin,admin123,admin");
                    writer.newLine();
                    System.out.println("✅ Đã tạo tài khoản admin mặc định: admin / admin123");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ✅ Xử lý Client
    static class ClientHandler implements Runnable {
        private Socket socket;

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
                System.out.println("Yêu cầu từ client: " + request);

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
                            String username = parts[1];
                            String password = parts[2];
                            String role = parts.length > 3 ? parts[3] : "user";
                            if (registerUser(username, password, role)) {
                                out.println("SUCCESS");
                            } else {
                                out.println("FAIL");
                            }
                            break;
                        }
                        case "DELETE": {
                            String usernameToDelete = parts[1];
                            if (deleteUser(usernameToDelete)) {
                                out.println("DELETE_SUCCESS");
                            } else {
                                out.println("DELETE_FAIL");
                            }
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

        // ✅ Kiểm tra đăng nhập
        private String checkLogin(String username, String password) throws IOException {
            File file = new File(USER_FILE);
            if (!file.exists()) return null;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals(username) && parts[1].equals(password)) {
                        return parts.length > 2 ? parts[2] : "user";
                    }
                }
            }
            return null;
        }

        // ✅ Gửi danh sách user
        private void sendUserList(PrintWriter out) throws IOException {
            File file = new File(USER_FILE);
            if (!file.exists()) {
                out.println("END");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                out.println("USERLIST_START");
                while ((line = reader.readLine()) != null) {
                    out.println(line);
                }
                out.println("END");
            }
        }

        // ✅ Đăng ký user
        private boolean registerUser(String username, String password, String role) throws IOException {
            File file = new File(USER_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals(username)) {
                        return false; // Username tồn tại
                    }
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(username + "," + password + "," + role);
                writer.newLine();
            }
            return true;
        }

        // ✅ Xóa user
        private boolean deleteUser(String usernameToDelete) throws IOException {
            if ("admin".equalsIgnoreCase(usernameToDelete)) return false;

            File file = new File(USER_FILE);
            if (!file.exists()) return false;

            List<String> users = new ArrayList<>();
            boolean found = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (!parts[0].equals(usernameToDelete)) {
                        users.add(line);
                    } else {
                        found = true;
                    }
                }
            }

            if (!found) return false;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (String user : users) {
                    writer.write(user);
                    writer.newLine();
                }
            }

            System.out.println("✅ Đã xóa user: " + usernameToDelete);
            return true;
        }
    }
}
