package hethong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginForm {
    private MainApp app;
    private Scene scene;

    public LoginForm(MainApp app) {
        this.app = app;
        createUI();
    }

    private void createUI() {
        Label title = new Label("Hello ladies and gentlemen");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Username or email address");
        emailField.setPrefWidth(280);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setPrefWidth(280);

        Hyperlink forgotLink = new Hyperlink("Forgot password?");
        forgotLink.setStyle("-fx-font-size: 12px;");
        forgotLink.setOnAction(e -> handleForgotPassword());
        
        


        Button signInBtn = new Button("Sign in");
        signInBtn.setStyle(
            "-fx-background-color: #2ea44f; -fx-text-fill: white; -fx-font-weight: bold;"
            + "-fx-background-radius: 6; -fx-pref-width: 280px; -fx-pref-height: 35px;"
        );

        signInBtn.setOnAction(e -> {
            String user = emailField.getText().trim();
            String pass = passField.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ thông tin đăng nhập!").showAndWait();
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                var ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
                ps.setString(1, user);
                ps.setString(2, pass);
                var rs = ps.executeQuery();

                if (rs.next()) {
                	String username = rs.getString("username");
                    String role = rs.getString("role");
                    String status = rs.getString("status"); // ✅ lấy đúng từ DB
                    String question = rs.getString("security_question");
                    String answer = rs.getString("security_answer");
                    
                 // 🧠 Kiểm tra nếu người dùng chưa có câu hỏi bảo mật
                    if (question == null || question.trim().isEmpty() || answer == null || answer.trim().isEmpty()) {
                        showSecurityUpdateForm(username);
                        return; // Dừng không cho vào dashboard
                    }

                    // ✅ Kiểm tra tài khoản bị cấm
                    if ("banned".equalsIgnoreCase(status)) {
                        new Alert(Alert.AlertType.ERROR,
                            "Tài khoản của bạn đã bị cấm.\nVui lòng liên hệ quản trị viên để được hỗ trợ!"
                        ).showAndWait();
                        return;
                    }

                    // ✅ Cập nhật loginStatus = 'Online' khi đăng nhập thành công
                    try (PreparedStatement updatePs = conn.prepareStatement(
                            "UPDATE users SET loginStatus = ? WHERE username = ?")) {
                        updatePs.setString(1, "Online");
                        updatePs.setString(2, user);
                        updatePs.executeUpdate();
                    }

                    // ✅ Mở dashboard phù hợp
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        AdminDashboard adminDashboard = new AdminDashboard(app);
                        app.setScene(adminDashboard.getScene());
                    } else {
                        User u = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("email"),
                            rs.getString("status"),       // cột status trong DB
                            "Online"                      // ✅ vừa cập nhật trạng thái
                        );

                        UserDashboard userDashboard = new UserDashboard(app, u);
                        app.setScene(userDashboard.getScene());
                    }

                } else {
                    new Alert(Alert.AlertType.ERROR, "Sai tên đăng nhập hoặc mật khẩu!").showAndWait();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Lỗi khi đăng nhập!").showAndWait();
            }
        });

        Label orLabel = new Label("or");
        orLabel.setStyle("-fx-text-fill: gray;");

        Button googleBtn = new Button("Continue with Google");
        googleBtn.setPrefWidth(280);

        Hyperlink signUpLink = new Hyperlink("Create an account");
        signUpLink.setOnAction(e -> app.showRegister());

        VBox layout = new VBox(10, title, emailField, passField, forgotLink, signInBtn, orLabel, googleBtn, signUpLink);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        scene = new Scene(layout, 400, 400);
    }

    public void handleForgotPassword() {
        // Hộp thoại lấy tên người dùng hoặc email
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Quên mật khẩu");
        dialog.setHeaderText("Khôi phục mật khẩu");
        dialog.setContentText("Nhập tên người dùng hoặc email của bạn:");

        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()) return;

        String input = result.get().trim();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Tìm user trong database
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT username, security_question, security_answer FROM users WHERE username = ? OR email = ?"
            );
            stmt.setString(1, input);
            stmt.setString(2, input);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Không tìm thấy tài khoản!");
                return;
            }

            String username = rs.getString("username");
            String question = rs.getString("security_question");
            String correctAnswer = rs.getString("security_answer");

            // Hiển thị câu hỏi bảo mật
            TextInputDialog answerDialog = new TextInputDialog();
            answerDialog.setTitle("Xác minh bảo mật");
            answerDialog.setHeaderText("Câu hỏi: " + question);
            answerDialog.setContentText("Câu trả lời của bạn:");

            Optional<String> answerResult = answerDialog.showAndWait();
            if (!answerResult.isPresent()) return;

            String userAnswer = answerResult.get().trim();

            if (!userAnswer.equalsIgnoreCase(correctAnswer)) {
                showAlert(Alert.AlertType.ERROR, "❌ Câu trả lời sai!");
                return;
            }

            // Cho phép đổi mật khẩu
            TextInputDialog newPassDialog = new TextInputDialog();
            newPassDialog.setTitle("Đặt lại mật khẩu");
            newPassDialog.setHeaderText("Nhập mật khẩu mới cho tài khoản " + username + ":");
            newPassDialog.setContentText("Mật khẩu mới:");

            Optional<String> newPassResult = newPassDialog.showAndWait();
            if (!newPassResult.isPresent()) return;

            String newPassword = newPassResult.get().trim();
            if (newPassword.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Mật khẩu không được để trống!");
                return;
            }

            // Cập nhật mật khẩu trong DB
            PreparedStatement updateStmt = conn.prepareStatement(
                "UPDATE users SET password = ? WHERE username = ?"
            );
            updateStmt.setString(1, newPassword);
            updateStmt.setString(2, username);
            updateStmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "✅ Mật khẩu đã được đặt lại thành công!");

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi khi khôi phục mật khẩu!");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showSecurityUpdateForm(String username) {
        Stage stage = new Stage();
        stage.setTitle("Cập nhật câu hỏi bảo mật");

        Label questionLabel = new Label("Chọn câu hỏi bảo mật:");
        ComboBox<String> questionBox = new ComboBox<>();
        questionBox.getItems().addAll(
            "Tên người bạn thân nhất của bạn?",
            "Món ăn bạn yêu thích?",
            "Nơi bạn sinh ra?",
            "Tên thú cưng đầu tiên của bạn?"
        );
        questionBox.setValue("Tên người bạn thân nhất của bạn?");

        Label answerLabel = new Label("Câu trả lời:");
        TextField answerField = new TextField();

        Button saveBtn = new Button("Lưu");
        Label statusLabel = new Label();

        saveBtn.setOnAction(e -> {
            String question = questionBox.getValue();
            String answer = answerField.getText().trim();

            if (answer.isEmpty()) {
                statusLabel.setText("⚠️ Vui lòng nhập câu trả lời!");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET security_question = ?, security_answer = ? WHERE username = ?"
                );
                stmt.setString(1, question);
                stmt.setString(2, answer);
                stmt.setString(3, username);
                stmt.executeUpdate();

                statusLabel.setText("✅ Cập nhật thành công!");
                statusLabel.setStyle("-fx-text-fill: green;");

                // Đóng form sau 1 giây
                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    javafx.application.Platform.runLater(stage::close);
                }).start();

            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("❌ Lỗi khi cập nhật!");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        VBox vbox = new VBox(10, questionLabel, questionBox, answerLabel, answerField, saveBtn, statusLabel);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        stage.setScene(new Scene(vbox, 400, 250));
        stage.show();
    }



	public Scene getScene() {
        return scene;
    }
}
