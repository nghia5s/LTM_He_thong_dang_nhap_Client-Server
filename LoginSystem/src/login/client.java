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

    public client() {
        setTitle("Login System");
        setSize(400, 300);
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

        loginBtn.addActionListener(e -> sendRequest("LOGIN",
                loginUsernameField.getText(),
                new String(loginPasswordField.getPassword())));

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

        JButton registerBtn = createStyledButton("Register");
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(registerBtn, gbc);

        registerBtn.addActionListener(e -> handleRegister());

        return panel;
    }

    private void handleRegister() {
        String username = registerUsernameField.getText();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp!");
            return;
        }
        sendRequest("REGISTER", username, password);
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

    private void sendRequest(String action, String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        try (
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            out.println(action + "|" + username + "|" + password);
            String response = in.readLine();

            if ("SUCCESS:ADMIN".equalsIgnoreCase(response)) {
                JOptionPane.showMessageDialog(this, "Admin đăng nhập thành công!");
                showAdminDashboard();
            } else if ("SUCCESS".equalsIgnoreCase(response)) {
                JOptionPane.showMessageDialog(this, action + " thành công!");
            } else {
                JOptionPane.showMessageDialog(this, action + " thất bại!");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối đến server.");
        }
    }

    // 🔥 Hiển thị danh sách user + Xóa User
    private void showAdminDashboard() {
        JFrame adminFrame = new JFrame("Admin Dashboard");
        adminFrame.setSize(500, 400);
        adminFrame.setLocationRelativeTo(null);

        String[] columnNames = {"Username", "Password", "Role"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton refreshBtn = createStyledButton("Làm mới");
        JButton deleteBtn = createStyledButton("Xóa User");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshBtn);
        buttonPanel.add(deleteBtn);

        adminFrame.setLayout(new BorderLayout());
        adminFrame.add(scrollPane, BorderLayout.CENTER);
        adminFrame.add(buttonPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadUserList(tableModel));
        deleteBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(adminFrame, "Chọn user cần xóa!");
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
            out.println("LOGIN|admin|admin123");
            String response = in.readLine();
            if (!"SUCCESS:ADMIN".equalsIgnoreCase(response)) return;

            String line;
            while (!(line = in.readLine()).equals("END")) {
                tableModel.addRow(line.split(","));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải danh sách user");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new client().setVisible(true));
    }
}
