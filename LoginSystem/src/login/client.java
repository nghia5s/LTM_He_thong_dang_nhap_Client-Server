package login;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;

public class client extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;
    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullnameField;
    private JTextField dobField;
    private JTextField phoneField;
    private JTextField emailField;

    public client() {
        setTitle("Login System");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Login", createLoginPanel());
        tabbedPane.addTab("Register", createRegisterPanel());
        add(tabbedPane);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(userLabel, gbc);

        loginUsernameField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(loginUsernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(passLabel, gbc);

        loginPasswordField = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(loginPasswordField, gbc);

        JButton loginBtn = createStyledButton("Login");
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> sendLoginRequest());

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(userLabel, gbc);

        registerUsernameField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(registerUsernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(passLabel, gbc);

        registerPasswordField = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(registerPasswordField, gbc);

        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(confirmPassLabel, gbc);

        confirmPasswordField = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(confirmPasswordField, gbc);

        JLabel fullnameLabel = new JLabel("Full Name:");
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(fullnameLabel, gbc);

        fullnameField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(fullnameField, gbc);

        JLabel dobLabel = new JLabel("Date of Birth (dd/MM/yyyy):");
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(dobLabel, gbc);

        dobField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(dobField, gbc);

        JLabel phoneLabel = new JLabel("Phone:");
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(phoneLabel, gbc);

        phoneField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 5;
        panel.add(phoneField, gbc);

        JLabel emailLabel = new JLabel("Email:");
        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(emailLabel, gbc);

        emailField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 6;
        panel.add(emailField, gbc);

        JButton registerBtn = createStyledButton("Register");
        gbc.gridx = 1; gbc.gridy = 7;
        panel.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> handleRegister());

        return panel;
    }

    private void handleRegister() {
        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String fullname = fullnameField.getText().trim();
        String dob = dobField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()
                || fullname.isEmpty() || dob.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
            return;
        }

        sendRegisterRequest(username, password, fullname, dob, phone, email);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
        return button;
    }

    private void sendLoginRequest() {
        String username = loginUsernameField.getText();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println("LOGIN|" + username + "|" + password);
            String response = in.readLine();

            if ("SUCCESS:ADMIN".equalsIgnoreCase(response)) {
                JOptionPane.showMessageDialog(this, "Admin đăng nhập thành công!");
                showAdminDashboard();
            } else if ("SUCCESS".equalsIgnoreCase(response)) {
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Đăng nhập thất bại!");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server.");
        }
    }

    private void sendRegisterRequest(String username, String password,
                                     String fullname, String dob, String phone, String email) {
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println("REGISTER|" + username + "|" + password + "|user|" + fullname + "|" + dob + "|" + phone + "|" + email);
            String response = in.readLine();

            if ("SUCCESS".equalsIgnoreCase(response)) {
                JOptionPane.showMessageDialog(this, "Đăng ký thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Đăng ký thất bại!");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server.");
        }
    }

    private void showAdminDashboard() {
        JFrame adminFrame = new JFrame("Admin Dashboard");
        adminFrame.setSize(900, 500);
        adminFrame.setLocationRelativeTo(null);

        String[] columnNames = {"Username", "Password", "Role", "Full Name", "Date of Birth", "Phone", "Email"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // ===== Các nút chức năng =====
        JButton addBtn = createStyledButton("Thêm người dùng");
        JButton refreshBtn = createStyledButton("Làm mới");
        JButton deleteBtn = createStyledButton("Xóa người dùng");
        JButton editBtn = createStyledButton("Sửa thông tin");

        // ===== Ô tìm kiếm =====
        JTextField searchField = new JTextField(15);
        JButton searchBtn = createStyledButton("Tìm kiếm");

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Tìm kiếm:"));
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(addBtn);
        topPanel.add(refreshBtn);
        topPanel.add(deleteBtn);
        topPanel.add(editBtn);

        adminFrame.setLayout(new BorderLayout());
        adminFrame.add(topPanel, BorderLayout.NORTH);
        adminFrame.add(scrollPane, BorderLayout.CENTER);

        // ===== Chức năng =====
        refreshBtn.addActionListener(e -> loadUserList(tableModel));

        addBtn.addActionListener(e -> {
            JTextField[] fields = new JTextField[7];
            JPanel panel = new JPanel(new GridLayout(7, 2));
            String[] labels = {"Username", "Password", "Role", "Full Name", "Date of Birth", "Phone", "Email"};
            for (int i = 0; i < 7; i++) {
                panel.add(new JLabel(labels[i] + ":"));
                fields[i] = new JTextField();
                panel.add(fields[i]);
            }

            int result = JOptionPane.showConfirmDialog(adminFrame, panel,
                    "Thêm người dùng mới", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                addUser(fields[0].getText(), fields[1].getText(), fields[2].getText(),
                        fields[3].getText(), fields[4].getText(), fields[5].getText(), fields[6].getText());
                loadUserList(tableModel);
            }
        });

        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(adminFrame, "Chọn người dùng cần xóa!");
                return;
            }
            String username = tableModel.getValueAt(selectedRow, 0).toString();
            if (username.equalsIgnoreCase("admin")) {
                JOptionPane.showMessageDialog(adminFrame, "Không thể xóa tài khoản admin!");
                return;
            }
            deleteUser(username);
            loadUserList(tableModel);
        });

        editBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(adminFrame, "Chọn người dùng cần sửa!");
                return;
            }
            JTextField[] fields = new JTextField[7];
            JPanel panel = new JPanel(new GridLayout(7, 2));
            String[] labels = {"Username", "Password", "Role", "Full Name", "Date of Birth", "Phone", "Email"};
            for (int i = 0; i < 7; i++) {
                panel.add(new JLabel(labels[i] + ":"));
                fields[i] = new JTextField(tableModel.getValueAt(selectedRow, i).toString());
                panel.add(fields[i]);
            }

            int result = JOptionPane.showConfirmDialog(adminFrame, panel,
                    "Sửa thông tin người dùng", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                updateUser(tableModel.getValueAt(selectedRow, 0).toString(),
                        fields[0].getText(), fields[1].getText(), fields[2].getText(),
                        fields[3].getText(), fields[4].getText(), fields[5].getText(), fields[6].getText());
                loadUserList(tableModel);
            }
        });

        // ===== Tìm kiếm =====
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            if (keyword.isEmpty()) {
                loadUserList(tableModel);
                return;
            }
            for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
                boolean match = false;
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    String cellValue = tableModel.getValueAt(i, j).toString().toLowerCase();
                    if (cellValue.contains(keyword)) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    tableModel.removeRow(i);
                }
            }
        });

        loadUserList(tableModel);
        adminFrame.setVisible(true);
    }



    private void loadUserList(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println("GET_ALL_USERS");
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("USERLIST_START")) continue;
                if (line.equals("END")) break;
                tableModel.addRow(line.split(","));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách người dùng");
        }
    }

    private void addUser(String username, String password, String role, String fullname, String dob, String phone, String email) {
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println("REGISTER|" + username + "|" + password + "|" + role + "|" + fullname + "|" + dob + "|" + phone + "|" + email);
            String response = in.readLine();
            JOptionPane.showMessageDialog(this, response.equals("SUCCESS")
                    ? "Thêm người dùng thành công!" : "Thêm người dùng thất bại!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối server!");
        }
    }

    private void deleteUser(String username) {
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println("DELETE|" + username);
            String response = in.readLine();
            JOptionPane.showMessageDialog(this, response.equals("DELETE_SUCCESS")
                    ? "Xóa thành công!" : "Xóa thất bại!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối server!");
        }
    }

    private void updateUser(String oldUsername, String newUsername, String newPassword, String newRole, String fullname, String dob, String phone, String email) {
        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println("UPDATE|" + oldUsername + "|" + newUsername + "|" + newPassword + "|" + newRole + "|" + fullname + "|" + dob + "|" + phone + "|" + email);
            String response = in.readLine();
            JOptionPane.showMessageDialog(this, response.equals("UPDATE_SUCCESS")
                    ? "Sửa thông tin thành công!" : "Sửa thông tin thất bại!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối server!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new client().setVisible(true));
    }
}
