package hethong;

import java.sql.Connection;
import java.sql.PreparedStatement;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;


public class UserDashboard {

    private Scene scene;
    private MainApp app;
    private User currentUser;

    public UserDashboard(MainApp app, User user) {
        this.app = app;
        this.currentUser = user;

     // ===== Avatar =====
        ImageView avatar = new ImageView();
        avatar.setFitWidth(100);
        avatar.setFitHeight(100);
        avatar.setPreserveRatio(true);

        // Load avatar từ user hoặc mặc định
        String avatarPath = currentUser.getAvatarPath();
        if (avatarPath == null || avatarPath.isEmpty()) {
            avatarPath = "file:avatar.png"; // file mặc định trong thư mục dự án
        }
        avatar.setImage(new Image(avatarPath));

        avatar.setClip(new javafx.scene.shape.Circle(50, 50, 50)); // bo tròn
        avatar.setStyle("-fx-cursor: hand;");

        // Hiệu ứng hover
        avatar.setOnMouseEntered(e -> avatar.setOpacity(0.8));
        avatar.setOnMouseExited(e -> avatar.setOpacity(1.0));

        
        // ===== Nút đổi ảnh đại diện =====
        Button btnChangeAvatar = new Button("Đổi ảnh đại diện");
        btnChangeAvatar.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Chọn ảnh đại diện");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            File selectedFile = fileChooser.showOpenDialog(app.getPrimaryStage());
            if (selectedFile != null) {
                String newPath = selectedFile.toURI().toString();
                currentUser.setAvatarPath(newPath);
                avatar.setImage(new Image(newPath));

                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE users SET avatarPath = ? WHERE id = ?"
                    );
                    ps.setString(1, newPath);
                    ps.setInt(2, currentUser.getId());
                    ps.executeUpdate();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });



        // ===== Thông tin cá nhân =====
        Label lblUsername = new Label("👤 Username: " + user.getUsername());
        lblUsername.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label lblEmail = new Label("📧 Email: " + user.getEmail());
        Label lblRole = new Label("💼 Role: " + user.getRole());
        Label lblStatus = new Label();
        updateStatusLabel(lblStatus, user.getLoginStatus());

        VBox infoBox = new VBox(10, lblUsername, lblEmail, lblRole, lblStatus);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        VBox avatarBox = new VBox(10, avatar, btnChangeAvatar);
        avatarBox.setAlignment(Pos.CENTER);

        HBox topBox = new HBox(20, avatarBox, infoBox);
        topBox.setAlignment(Pos.CENTER_LEFT);


     // ===== Nút chức năng (hiệu ứng nhẹ, màu đồng bộ) =====
        Button btnLogout = new Button("Đăng xuất");
        btnLogout.setStyle("""
            -fx-background-color: linear-gradient(to right, #ff5f6d, #ff4d4d);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 20;
        """);
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle("""
            -fx-background-color: linear-gradient(to right, #ff7a7a, #ff5f6d);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 20;
        """));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle("""
            -fx-background-color: linear-gradient(to right, #ff5f6d, #ff4d4d);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 20;
        """));
        btnLogout.setOnAction(e -> logout());


        Button btnEditInfo = new Button("Sửa thông tin / Đổi mật khẩu");
        btnEditInfo.setStyle("""
            -fx-background-color: linear-gradient(to right, #56ab2f, #a8e063);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 20;
        """);
        btnEditInfo.setOnMouseEntered(e -> btnEditInfo.setStyle("""
            -fx-background-color: linear-gradient(to right, #76c93a, #b5e87a);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 20;
        """));
        btnEditInfo.setOnMouseExited(e -> btnEditInfo.setStyle("""
            -fx-background-color: linear-gradient(to right, #56ab2f, #a8e063);
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-padding: 8 20;
        """));
        btnEditInfo.setOnAction(e -> openEditDialog());

        HBox buttonBox = new HBox(20, btnEditInfo, btnLogout);
        buttonBox.setAlignment(Pos.CENTER);


     // Nền gradient + bo góc + bóng nhẹ
        VBox root = new VBox(30, topBox, buttonBox);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("""
            -fx-background-color: linear-gradient(to bottom right, #e3f2fd, #bbdefb);
        """);

        // Thêm card hiển thị thông tin
        VBox card = new VBox(20, topBox, buttonBox);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(25));
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);
        """);

        root.getChildren().add(card);
        scene = new Scene(root, 600, 400);

    }

    // ===== Hàm cập nhật status =====
    private void updateStatusLabel(Label lbl, String status) {
        if ("Online".equalsIgnoreCase(status)) {
        	lbl.setText("🟢 Trạng thái: Online");
        	lbl.setStyle("""
        	    -fx-font-weight: bold;
        	    -fx-text-fill: linear-gradient(to right, #4CAF50, #81C784);
        	""");

        	// Hiệu ứng nhấp nháy nhẹ
        	javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(1.2), lbl);
        	ft.setFromValue(1.0);
        	ft.setToValue(0.7);
        	ft.setCycleCount(javafx.animation.Animation.INDEFINITE);
        	ft.setAutoReverse(true);
        	ft.play();

        } else {
            lbl.setText("⚪ Trạng thái: Offline");
            lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: gray;");
        }
    }

    // ===== Logout =====
    private void logout() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET loginStatus = ? WHERE username = ?"
            );
            ps.setString(1, "Offline");
            ps.setString(2, currentUser.getUsername());
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        app.showLogin();
    }

    // ===== Dialog sửa thông tin / đổi mật khẩu =====
    private void openEditDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin / Đổi mật khẩu");

        TextField emailField = new TextField(currentUser.getEmail());
        ChoiceBox<String> roleChoice = new ChoiceBox<>(FXCollections.observableArrayList("user", "admin"));
        roleChoice.setValue(currentUser.getRole());
        roleChoice.setDisable(true); // Không cho chỉnh vai trò
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Để trống nếu không muốn đổi mật khẩu");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.addRow(0, new Label("Email:"), emailField);
        grid.addRow(1, new Label("Role:"), roleChoice);
        grid.addRow(2, new Label("Password:"), passwordField);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveType = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                String email = emailField.getText().trim();
                String role = roleChoice.getValue();
                String newPassword = passwordField.getText().trim();

                if (email.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Email không được để trống!").showAndWait();
                    return null;
                }

                try (Connection conn = DatabaseConnection.getConnection()) {
                    String sql;
                    if (!newPassword.isEmpty()) {
                        sql = "UPDATE users SET email = ?, role = ?, password = ? WHERE id = ?";
                    } else {
                        sql = "UPDATE users SET email = ?, role = ? WHERE id = ?";
                    }

                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, email);
                    stmt.setString(2, role);
                    if (!newPassword.isEmpty()) {
                        stmt.setString(3, newPassword);
                        stmt.setInt(4, currentUser.getId());
                    } else {
                        stmt.setInt(3, currentUser.getId());
                    }
                    stmt.executeUpdate();

                    // Cập nhật thông tin hiện tại
                    currentUser.setEmail(email);
                    currentUser.setRole(role);
                    if (!newPassword.isEmpty()) currentUser.setPassword(newPassword);

                    new Alert(Alert.AlertType.INFORMATION, "Cập nhật thành công!").showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Lỗi khi cập nhật!").showAndWait();
                    ex.printStackTrace();
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
