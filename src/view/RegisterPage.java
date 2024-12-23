package view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import ip.IP;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Register");

        // Nhãn tiêu đề "ĐĂNG KÝ"
        Label registerLabel = new Label("Register");
        registerLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        // Trường nhập liệu
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);
        usernameField.setStyle("-fx-font-size: 16px;");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(300);
        emailField.setStyle("-fx-font-size: 16px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-font-size: 16px;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setMaxWidth(300);
        confirmPasswordField.setStyle("-fx-font-size: 16px;");

        // Nút đăng ký và quay lại
        Button btnRegister = new Button("Register");
        Button btnBack = new Button("Back");

        btnRegister.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        btnBack.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px;");

        // Xử lý sự kiện cho nút đăng ký
        btnRegister.setOnAction(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("Please fill in all fields!");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert("Passwords do not match!");
                return;
            }

            // Tạo URL và kết nối đến server
            String url = IP.SERVER_IP + "/register";
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Tạo dữ liệu để gửi
                String requestData = "username=" + URLEncoder.encode(username, "UTF-8") + "&email=" + URLEncoder.encode(email, "UTF-8") 
                + "&password=" + URLEncoder.encode(password, "UTF-8");

                // Gửi dữ liệu
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(requestData.getBytes());
                    os.flush();
                }

                // Nhận phản hồi từ server
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String response = in.readLine();

                    if ("success".equals(response)) {
                        showAlert("Registration successful!");
                        new LoginPage().start(primaryStage);
                    } else {
                        showAlert("Registration failed");
                    }
                } else {
                    showAlert("Registration failed");
                }

                connection.disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
                showAlert("Error connecting to server.");
            }
        });

        // Xử lý sự kiện cho nút quay lại
        btnBack.setOnAction(e -> {
            new LoginPage().start(primaryStage);
        });

        // Layout cho phần đăng ký
        VBox layout = new VBox(15, registerLabel, usernameField, emailField, passwordField, confirmPasswordField, btnRegister, btnBack);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: #f0f0f0;");
        layout.setMaxWidth(600);

        // Tạo Scene và đặt kích thước
        Scene scene = new Scene(layout, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Phương thức hiển thị thông báo
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Phương thức khởi chạy ứng dụng
    public static void main(String[] args) {
        launch(args);
    }
}
