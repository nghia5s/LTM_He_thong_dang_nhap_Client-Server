package hethong;

import javafx.beans.property.*;

public class User {
    private final IntegerProperty id;
    private final StringProperty username;
    private final StringProperty password;
    private final StringProperty role;
    private final StringProperty email;
    private final StringProperty status;
    private final StringProperty loginStatus;
    private final StringProperty avatarPath = new SimpleStringProperty("file:avatar.png"); // mặc định


    // ✅ Constructor đầy đủ 7 tham số
    public User(int id, String username, String password, String role, String email, String status, String loginStatus) {
        this.id = new SimpleIntegerProperty(id);
        this.username = new SimpleStringProperty(username);
        this.password = new SimpleStringProperty(password);
        this.role = new SimpleStringProperty(role);
        this.email = new SimpleStringProperty(email);
        this.status = new SimpleStringProperty(status);
        this.loginStatus = new SimpleStringProperty(loginStatus);
    }

    // ===== Getters =====
    public int getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getPassword() { return password.get(); }
    public String getRole() { return role.get(); }
    public String getEmail() { return email.get(); }
    public String getStatus() { return status.get(); }
    public String getLoginStatus() { return loginStatus.get(); }
    public String getAvatarPath() {
        return avatarPath.get();
    }

    // ===== Property Getters =====
    public IntegerProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty roleProperty() { return role; }
    public StringProperty emailProperty() { return email; }
    public StringProperty statusProperty() { return status; }
    public StringProperty loginStatusProperty() { return loginStatus; }

    // ===== Setters =====
    public void setStatus(String value) { status.set(value); }
    public void setLoginStatus(String value) { loginStatus.set(value); }
 // Trong class User
    public void setEmail(String value) {
        email.set(value);
    }

    public void setRole(String value) {
        role.set(value);
    }

    public void setPassword(String value) {
        password.set(value);
    }
    public void setAvatarPath(String path) {
        avatarPath.set(path);
    }

}
