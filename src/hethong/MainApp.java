package hethong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Stage mainStage;
    private Scene loginScene;
    private Scene registerScene;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;

        // Tạo form login và register
        LoginForm loginForm = new LoginForm(this);
        RegisterForm registerForm = new RegisterForm(this);

        loginScene = loginForm.getScene();
        registerScene = registerForm.getScene();

        mainStage.setScene(loginScene);
        mainStage.setTitle("GitHub Style Login/Register");
        mainStage.show();
    }

    // ==== Các hàm chuyển scene có sẵn ====
    public void showRegister() {
        mainStage.setScene(registerScene);
    }

    public void showLogin() {
        mainStage.setScene(loginScene);
    }

    // ==== Hàm hỗ trợ mới ====

    // Mở dashboard của admin
    public void showAdminDashboard(String username) {
        AdminDashboard adminDashboard = new AdminDashboard(this);
        mainStage.setScene(adminDashboard.getScene());
        mainStage.setTitle("Bảng điều khiển Admin - " + username);
    }

    // Mở dashboard của user
    public void showUserDashboard(String username) {
        User user = null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT id, username, password, role, email, status, loginStatus FROM users WHERE username = ?"
            );
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("status"),
                    rs.getString("loginStatus")
                );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (user != null) {
            UserDashboard userDashboard = new UserDashboard(this, user); // dùng constructor (MainApp, User)
            mainStage.setScene(userDashboard.getScene());
            mainStage.setTitle("Trang người dùng - " + username);
        } else {
            // User không tìm thấy (nếu có lỗi)
            new Alert(Alert.AlertType.ERROR, "Không tìm thấy thông tin user!").showAndWait();
        }
    }


    // Hàm tiện ích để set scene trực tiếp (giữ nguyên của bạn)
    public void setScene(Scene scene) {
        mainStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}
