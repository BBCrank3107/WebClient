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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterPage extends Application {

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Register");

		TextField usernameField = new TextField();
		usernameField.setPromptText("Username");

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Password");

		PasswordField confirmPasswordField = new PasswordField();
		confirmPasswordField.setPromptText("Confirm Password");

		Button btnRegister = new Button("Register");
		Button btnBack = new Button("Back");

		btnRegister.setOnAction(e -> {
			String username = usernameField.getText();
			String password = passwordField.getText();
			String confirmPassword = confirmPasswordField.getText();

			if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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
				String requestData = "username=" + URLEncoder.encode(username, "UTF-8") + "&password="
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

		btnBack.setOnAction(e -> {
			new LoginPage().start(primaryStage);
		});

		VBox layout = new VBox(10, usernameField, passwordField, confirmPasswordField, btnRegister, btnBack);
		Scene scene = new Scene(layout, 300, 250);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
