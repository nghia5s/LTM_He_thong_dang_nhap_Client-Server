package hethong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class RegisterForm {
    private MainApp app;
    private Scene scene;

    // 🟢 Biến toàn cục cho các trường nhập liệu
    private TextField emailField;
    private PasswordField passField;
    private TextField userField;
    private Label messageLabel;
    private ComboBox<String> securityQuestionBox;
    private TextField securityAnswerField;


    public RegisterForm(MainApp app) {
        this.app = app;
        createUI();
    }

    private void createUI() {
        Label title = new Label("Sign up for GitHub");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button googleBtn = new Button("Continue with Google");
        googleBtn.setPrefWidth(280);

        // 🟢 Các trường nhập liệu
        emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefWidth(280);

        passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setPrefWidth(280);

        userField = new TextField();
        userField.setPromptText("Username");
        userField.setPrefWidth(280);

        ComboBox<String> countryBox = new ComboBox<>();
        countryBox.getItems().addAll("Vietnam", "Myanmar", "Thailand", "United States", "Other");
        countryBox.setValue("Vietnam");
        countryBox.setPrefWidth(280);
        
        Label questionLabel = new Label("Security question:");
        ComboBox<String> securityQuestionBox = new ComboBox<>();
        securityQuestionBox.getItems().addAll(
            "What is your favorite color?",
            "What is your pet's name?",
            "What is your mother's maiden name?",
            "What city were you born in?"
        );
        securityQuestionBox.setPrefWidth(280);

        PasswordField securityAnswerField = new PasswordField();
        securityAnswerField.setPromptText("Your answer");
        securityAnswerField.setPrefWidth(280);


        CheckBox emailPref = new CheckBox("Receive occasional product updates");

        // 🟢 Nút đăng ký
        Button createBtn = new Button("Create account");
        createBtn.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        createBtn.setPrefWidth(280);
        createBtn.setOnAction(e -> handleRegister());
        
        // 🟢 Nút quay lại
        Button backBtn = new Button("Back to Login");
        backBtn.setOnAction(e -> app.showLogin());

        // 🟢 Label hiển thị thông báo
        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        VBox form = new VBox(10,
            title,
            googleBtn,
            emailField,
            passField,
            userField,
            countryBox,
            emailPref,
            securityQuestionBox,
            securityAnswerField,
            createBtn,
            messageLabel,
            backBtn
        );
        form.setAlignment(Pos.CENTER_LEFT);
        form.setPadding(new Insets(20));

        HBox root = new HBox(form);
        root.setAlignment(Pos.CENTER);

        scene = new Scene(root, 500, 450);
    }

    // 🟢 Hàm xử lý đăng ký tài khoản
    private void handleRegister() {
        String username = userField.getText().trim();
        String password = passField.getText().trim();
        String email = emailField.getText().trim();
        String securityQuestion = securityQuestionBox.getValue();
        String securityAnswer = securityAnswerField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            messageLabel.setText("⚠️ Vui lòng nhập đầy đủ thông tin!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (securityQuestion == null || securityAnswer.isEmpty()) {
            messageLabel.setText("⚠️ Vui lòng chọn câu hỏi và nhập câu trả lời bảo mật!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }


        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                messageLabel.setText("⚠️ Tên người dùng đã tồn tại!");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            PreparedStatement insertStmt = conn.prepareStatement(
            	    "INSERT INTO users (username, password, email, role, status, security_question, security_answer) " +
            	    "VALUES (?, ?, ?, 'USER', 'active', ?, ?)"
            	);
            insertStmt.setString(1, username);
            insertStmt.setString(2, password);
            insertStmt.setString(3, email);
            insertStmt.setString(4, securityQuestion);
            insertStmt.setString(5, securityAnswer);
            insertStmt.executeUpdate();

            messageLabel.setText("✅ Tạo tài khoản thành công!");
            messageLabel.setStyle("-fx-text-fill: green;");
            
         // 🧹 Xóa dữ liệu sau khi đăng ký thành công
            userField.clear();
            passField.clear();
            emailField.clear();
            securityQuestionBox.setValue(null);
            securityAnswerField.clear();

            
         // ⏳ Quay lại màn hình đăng nhập sau 2 giây
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> app.showLogin());
            }).start();


        } catch (Exception ex) {
            ex.printStackTrace();
            messageLabel.setText("❌ Lỗi khi tạo tài khoản!");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    public Scene getScene() {
        return scene;
    }
}
