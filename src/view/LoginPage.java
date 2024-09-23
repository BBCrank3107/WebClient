package view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import ip.IP;

public class LoginPage extends Application {

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Login");

		TextField usernameField = new TextField();
		usernameField.setPromptText("Username");

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Password");

		Button btnLogin = new Button("Login");
		Button btnRegister = new Button("Register");

		btnLogin.setOnAction(e -> {
			String username = usernameField.getText();
			String password = passwordField.getText();

			String url = IP.SERVER_IP + "/login";
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
						showAlert("Login successful!");
						new ManagerPage(username).start(primaryStage);
					} else {
						showAlert("Login failed: " + response);
					}
				} else {
					showAlert("Username or Password Invalid!");
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

		VBox layout = new VBox(10, usernameField, passwordField, btnLogin, btnRegister);
		Scene scene = new Scene(layout, 300, 200);
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
