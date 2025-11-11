package hethong;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import java.sql.*;

public class AdminDashboard {
    private Scene scene;
    private TableView<User> table;
    private ObservableList<User> data = FXCollections.observableArrayList();
    private FilteredList<User> filteredData;
    private PieChart pieChart;

    public AdminDashboard(MainApp app) {
        // ===== PHẦN 1: BIỂU ĐỒ TRÒN =====
        pieChart = new PieChart();
        pieChart.setTitle("Thống kê người dùng");
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setClockwise(true);
        pieChart.setStartAngle(90);

        VBox chartBox = new VBox(pieChart);
        chartBox.setAlignment(Pos.CENTER);
        chartBox.setPadding(new Insets(10));

     // ===== PHẦN 2: Bộ lọc & tìm kiếm =====
        TextField searchField = new TextField();
        searchField.setPromptText("Tìm theo username hoặc email...");

        
        ChoiceBox<String> roleFilter = new ChoiceBox<>(
        	    FXCollections.observableArrayList("Tất cả", "admin", "user" )
        	);
        	roleFilter.setValue("Tất cả");


        Button clearFilterBtn = new Button("Xóa lọc");

        // Khi người dùng thay đổi text hoặc role → áp dụng bộ lọc
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(searchField, roleFilter));
        roleFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters(searchField, roleFilter));

        // Nút "Xóa lọc" để reset bộ lọc về mặc định
        clearFilterBtn.setOnAction(e -> {
            searchField.clear();
            roleFilter.setValue("Tất cả");
            applyFilters(searchField, roleFilter);
        });

        HBox filterBox = new HBox(10,
                new Label("Tìm kiếm:"), searchField,
                new Label("Lọc theo vai trò:"), roleFilter,
                clearFilterBtn
        );
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.setPadding(new Insets(5, 20, 10, 20));


     // ===== PHẦN 3: Bảng dữ liệu =====
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> c.getValue().idProperty().asObject());

        TableColumn<User, String> nameCol = new TableColumn<>("Username");
        nameCol.setCellValueFactory(c -> c.getValue().usernameProperty());

        TableColumn<User, String> passCol = new TableColumn<>("Mật khẩu"); // 🔹 thêm
        passCol.setCellValueFactory(c -> c.getValue().passwordProperty());

        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(c -> c.getValue().roleProperty());

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> c.getValue().emailProperty());

        TableColumn<User, String> statusCol = new TableColumn<>("Trạng thái");
        statusCol.setCellValueFactory(c -> c.getValue().statusProperty()); // 🔹 status trong DB

        TableColumn<User, String> loginStatusCol = new TableColumn<>("Đăng nhập"); // 🔹 thêm
        loginStatusCol.setCellValueFactory(c -> c.getValue().loginStatusProperty());

        // ✅ Thêm toàn bộ cột vào bảng
        table.getColumns().addAll(idCol, nameCol, passCol, roleCol, emailCol, statusCol, loginStatusCol);


        // Hiển thị màu theo trạng thái
        statusCol.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    setStyle("-fx-font-weight: bold; -fx-alignment: center;");
                    switch (status.toLowerCase()) {
                        case "hoạt động":
                            setTextFill(javafx.scene.paint.Color.GREEN);
                            break;
                        case "offline":
                            setTextFill(javafx.scene.paint.Color.RED);
                            break;
                        case "bị cấm":
                        case "banned":
                            setTextFill(javafx.scene.paint.Color.ORANGE);
                            break;
                        default:
                            setTextFill(javafx.scene.paint.Color.BLACK);
                    }
                }
            }
        });


     // Xóa toàn bộ cột cũ (nếu có)
        table.getColumns().clear();

        // Sau đó add một lần duy nhất
        table.getColumns().addAll(
            idCol,
            nameCol,
            passCol,
            roleCol,
            emailCol,
            statusCol,
            loginStatusCol
        );

        filteredData = new FilteredList<>(data, p -> true);

        SortedList<User> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // ===== PHẦN 4: Các nút thao tác =====
        Button addBtn = new Button("Thêm");
        Button editBtn = new Button("Sửa");
        Button deleteBtn = new Button("Xóa");
        Button refreshBtn = new Button("Làm mới");
        Button banBtn = new Button("Cấm hoạt động");
        Button unbanBtn = new Button("Gỡ cấm");
        Button logoutBtn = new Button("Đăng xuất");

        logoutBtn.setOnAction(e -> {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET loginStatus = 'Offline' WHERE role = 'admin'"
                );
                ps.executeUpdate();
                System.out.println("✅ Admin đã đăng xuất, cập nhật trạng thái Offline trong DB");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // Quay về màn hình đăng nhập
            LoginForm loginForm = new LoginForm(app);
            app.setScene(loginForm.getScene());
        });


        addBtn.setOnAction(e -> handleAddUser());
        editBtn.setOnAction(e -> handleEditUser());
        deleteBtn.setOnAction(e -> handleDeleteUser());
        refreshBtn.setOnAction(e -> loadUsersFromDatabase());
        banBtn.setOnAction(e -> handleBanUser());
        unbanBtn.setOnAction(e -> handleUnbanUser());

        // Bộ lọc
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters(searchField, roleFilter));
        roleFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters(searchField, roleFilter));
        clearFilterBtn.setOnAction(e -> {
            searchField.clear();
            roleFilter.setValue("Tất cả");
        });

     // ===== PHẦN 5: Layout tổng =====

     // Nhóm nút thao tác chính (Add/Edit/Delete/Refresh)
     HBox actionButtons = new HBox(10, addBtn, editBtn, deleteBtn, refreshBtn, banBtn, unbanBtn);
     actionButtons.setAlignment(Pos.CENTER_LEFT);
     actionButtons.setPadding(new Insets(5));

     // Nút đăng xuất tách riêng
     HBox logoutBox = new HBox(logoutBtn);
     logoutBox.setAlignment(Pos.CENTER_RIGHT);
     logoutBox.setPadding(new Insets(5));

     // Thanh công cụ phía trên cùng: bên trái là nút thao tác, bên phải là đăng xuất
     BorderPane topBar = new BorderPane();
     topBar.setLeft(actionButtons);
     topBar.setRight(logoutBox);

     // Trung tâm: bảng dữ liệu + bộ lọc
     VBox centerBox = new VBox(10, filterBox, table);
     centerBox.setPadding(new Insets(10));

     // Biểu đồ nằm bên phải
     VBox rightBox = new VBox(chartBox);
     rightBox.setPadding(new Insets(10));
     rightBox.setAlignment(Pos.CENTER);

     // Layout tổng dùng BorderPane
     BorderPane layout = new BorderPane();
     layout.setTop(topBar);
     layout.setCenter(centerBox);
     layout.setRight(rightBox);
     layout.setPadding(new Insets(15));

     scene = new Scene(layout, 1000, 600);
     loadUsersFromDatabase();
    }

    public Scene getScene() {
        return scene;
    }

    // ===== XỬ LÝ NÚT =====
    private void handleAddUser() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Thêm người dùng mới");

        Label l1 = new Label("Username:");
        TextField usernameField = new TextField();
        Label l2 = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        Label l3 = new Label("Email:");
        TextField emailField = new TextField();
        Label l4 = new Label("Role:");
        ChoiceBox<String> roleChoice = new ChoiceBox<>(FXCollections.observableArrayList("user", "admin"));
        roleChoice.setValue("user");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.addRow(0, l1, usernameField);
        grid.addRow(1, l2, passwordField);
        grid.addRow(2, l3, emailField);
        grid.addRow(3, l4, roleChoice);
        dialog.getDialogPane().setContent(grid);

        ButtonType addType = new ButtonType("Thêm", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == addType) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String email = emailField.getText().trim();
                String role = roleChoice.getValue();

                // ===== Validation =====
                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Vui lòng điền đầy đủ thông tin!").showAndWait();
                    return null;
                }

                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO users (username, password, email, role, status, loginStatus) VALUES (?, ?, ?, ?, 'active', 'offline')");
                    stmt.setString(1, username);
                    stmt.setString(2, password);
                    stmt.setString(3, email);
                    stmt.setString(4, role);
                    stmt.executeUpdate();

                    loadUsersFromDatabase();
                    new Alert(Alert.AlertType.INFORMATION, "Thêm tài khoản thành công!").showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Lỗi khi thêm người dùng!").showAndWait();
                    ex.printStackTrace();
                }
            }
            return null;
        });
        dialog.showAndWait();
    }

    private void handleEditUser() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn người dùng để sửa!").showAndWait();
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin người dùng");

        // Fields
        Label l1 = new Label("Email:");
        TextField emailField = new TextField(selected.getEmail());

        Label l2 = new Label("Role:");
        ChoiceBox<String> roleChoice = new ChoiceBox<>(FXCollections.observableArrayList("user", "admin"));
        roleChoice.setValue(selected.getRole());

        Label l3 = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Để trống nếu không muốn đổi mật khẩu");

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.addRow(0, l1, emailField);
        grid.addRow(1, l2, roleChoice);
        grid.addRow(2, l3, passwordField);
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
                        stmt.setInt(4, selected.getId());
                    } else {
                        stmt.setInt(3, selected.getId());
                    }

                    stmt.executeUpdate();
                    loadUsersFromDatabase();
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


    private void handleDeleteUser() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn người dùng để xóa!").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa người dùng này?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?");
                    stmt.setInt(1, selected.getId());
                    stmt.executeUpdate();
                    loadUsersFromDatabase();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Lỗi khi xóa người dùng!").showAndWait();
                    ex.printStackTrace();
                }
            }
        });
    }
    
    private void handleBanUser() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn người dùng để cấm!").showAndWait();
            return;
        }

        if ("banned".equalsIgnoreCase(selected.getStatus())) {
            new Alert(Alert.AlertType.INFORMATION, "Người dùng này đã bị cấm trước đó!").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn cấm người dùng này?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE users SET status = 'banned' WHERE id = ?");
                    stmt.setInt(1, selected.getId());
                    stmt.executeUpdate();
                    loadUsersFromDatabase();
                    new Alert(Alert.AlertType.INFORMATION, "Đã cấm người dùng thành công!").showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Lỗi khi cấm người dùng!").showAndWait();
                    ex.printStackTrace();
                }
            }
        });
    }

    private void handleUnbanUser() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn người dùng để gỡ cấm!").showAndWait();
            return;
        }

        if (!"banned".equalsIgnoreCase(selected.getStatus())) {
            new Alert(Alert.AlertType.INFORMATION, "Người dùng này không bị cấm!").showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn gỡ cấm người dùng này?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "UPDATE users SET status = 'active' WHERE id = ?");
                    stmt.setInt(1, selected.getId());
                    stmt.executeUpdate();
                    loadUsersFromDatabase();
                    new Alert(Alert.AlertType.INFORMATION, "Đã gỡ cấm người dùng!").showAndWait();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Lỗi khi gỡ cấm người dùng!").showAndWait();
                    ex.printStackTrace();
                }
            }
        });
    }
    

 // ===== LỌC (phiên bản mạnh mẽ + debug) =====
    private void applyFilters(TextField searchField, ChoiceBox<String> roleFilter) {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String selectedRoleRaw = roleFilter.getValue();
        String selectedRole = selectedRoleRaw == null ? "tất cả" : selectedRoleRaw.toLowerCase().trim();

        filteredData.setPredicate(user -> {
            if (user == null) return false;

            String username = user.getUsername() == null ? "" : user.getUsername().toLowerCase().trim();
            String email = user.getEmail() == null ? "" : user.getEmail().toLowerCase().trim();
            String role = user.getRole() == null ? "" : user.getRole().toLowerCase().trim();

            boolean matchesSearch = searchText.isEmpty() ||
                    username.contains(searchText) ||
                    email.contains(searchText);

            boolean matchesRole;
            if (selectedRole.equals("tất cả")) {
                matchesRole = true;
            } else {
                matchesRole = role.equals(selectedRole);
            }

            return matchesSearch && matchesRole;
        });
    }


    // ===== LOAD DỮ LIỆU & CẬP NHẬT BIỂU ĐỒ =====
    private void loadUsersFromDatabase() {
        data.clear();

        int countAll = 0, countAdmin = 0, countUser = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
        	String sql = "SELECT id, username, password, role, email, status, loginStatus FROM users";
        	PreparedStatement stmt = conn.prepareStatement(sql);
        	ResultSet rs = stmt.executeQuery();

        	while (rs.next()) {
        	    int id = rs.getInt("id");
        	    String username = rs.getString("username");
        	    String password = rs.getString("password");
        	    String role = rs.getString("role");
        	    String email = rs.getString("email");

        	    // ✅ Lấy đúng tên cột trong DB (status & loginStatus)
        	    String status = rs.getString("status");
        	    String loginStatus = rs.getString("loginStatus");

        	    if (status == null || status.isBlank()) status = "Hoạt động";
        	    if (loginStatus == null || loginStatus.isBlank()) loginStatus = "Offline";

        	    data.add(new User(id, username, password, role, email, status, loginStatus));
                countAll++;
                if ("admin".equalsIgnoreCase(role)) countAdmin++;
                else countUser++;
            }

            updatePieChart(countAdmin, countUser);

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không thể tải dữ liệu từ cơ sở dữ liệu!").showAndWait();
        }

     // Debug: in toàn bộ user vừa load (để xem role có extra whitespace hoặc khác gì không)
        for (User u : data) {
            System.out.println("LOAD DBG -> id=" + u.getId()
                    + " username='" + u.getUsername()
                    + "' roleRaw='" + u.getRole()
                    + "' email='" + u.getEmail() + "'");
        }

    }

    // ===== CẬP NHẬT BIỂU ĐỒ TRÒN =====
    private void updatePieChart(int adminCount, int userCount) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Admin", adminCount),
                new PieChart.Data("User", userCount)
        );
        pieChart.setData(pieData);
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
