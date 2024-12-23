package view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import ip.IP;

public class LoginPage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        Label loginLabel = new Label("LOGIN");
        loginLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setMaxWidth(300);
        emailField.setStyle("-fx-font-size: 16px;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-font-size: 16px;");

        Button btnLogin = new Button("Login");
        Button btnRegister = new Button("Register");

        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px;");
        btnRegister.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px;");

        btnLogin.setOnAction(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            String url = IP.SERVER_IP + "/login";
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Tạo dữ liệu để gửi
                String requestData = "email=" + URLEncoder.encode(email, "UTF-8") + "&password="
                        + URLEncoder.encode(password, "UTF-8");

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
                        showAlert("Login successful!");
                        new ManagerPage(email).start(primaryStage);
                    } else {
                        showAlert("Login failed: " + response);
                    }
                } else {
                    showAlert("Email or Password Invalid!");
                }

                connection.disconnect();
            } catch (IOException ex) {
                ex.printStackTrace();
                showAlert("Error connecting to server.");
            }
        });

        btnRegister.setOnAction(e -> {
            new RegisterPage().start(primaryStage);
        });

        // Layout cho phần đăng nhập
        VBox loginLayout = new VBox(15, loginLabel, emailField, passwordField, btnLogin, btnRegister);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(30));
        loginLayout.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 10; -fx-background-radius: 10;");
        loginLayout.setMaxWidth(600);

        // Phần hình ảnh bên trái
        ImageView imageView = new ImageView(new Image("file:images/login.jpg"));
        imageView.setFitWidth(600);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);

        StackPane imagePane = new StackPane(imageView);
        imagePane.setPrefWidth(600);
        imagePane.setAlignment(Pos.CENTER);

        HBox mainLayout = new HBox(imagePane, loginLayout);
        HBox.setHgrow(imagePane, Priority.ALWAYS);
        HBox.setHgrow(loginLayout, Priority.ALWAYS);
        mainLayout.setSpacing(0);
        mainLayout.setPrefHeight(800);

        Scene scene = new Scene(mainLayout, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
