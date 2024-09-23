package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ip.IP;

public class ManagerPage extends Application {

	private String username;
	private ListView<String> listView;

	public ManagerPage(String username) {
		this.username = username;
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Manager");

		listView = new ListView<>();
		Label lblUsername = new Label("Logged in as: " + username);
		Button btnAdd = new Button("Add Website");
		Button btnDelete = new Button("Delete Website");
		Button btnLogout = new Button("Logout");

		// Load file list when the window opens
		loadFileListFromServer();

		btnAdd.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
			File selectedFile = fileChooser.showOpenDialog(primaryStage);

			if (selectedFile != null) {
				try {
					uploadFileToServer(selectedFile);
					loadFileListFromServer();
				} catch (IOException ex) {
					showAlert("File upload failed: " + ex.getMessage());
				}
			}
		});

		btnDelete.setOnAction(e -> {
			String selectedFile = listView.getSelectionModel().getSelectedItem();
			if (selectedFile != null) {
				try {
					deleteFileFromServer(selectedFile);
					loadFileListFromServer();
				} catch (IOException ex) {
					showAlert("File deletion failed: " + ex.getMessage());
				}
			} else {
				showAlert("Please select a file to delete.");
			}
		});

		btnLogout.setOnAction(e -> {
			sendLogoutRequest();
			new LoginPage().start(primaryStage);
		});

		VBox layout = new VBox(10, lblUsername, listView, btnAdd, btnDelete, btnLogout);
		Scene scene = new Scene(layout, 300, 250);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	// Upload file to server
	private void uploadFileToServer(File file) throws IOException {
		String fileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8);
		String url = IP.SERVER_IP + "/upload?username=" + username + "&filename=" + fileName;

		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "application/octet-stream");

		try (OutputStream os = connection.getOutputStream(); FileInputStream fis = new FileInputStream(file)) {

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = fis.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
		}

		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			showAlert("File uploaded successfully!");
		} else {
			showAlert("File upload failed with HTTP code: " + responseCode);
		}

		connection.disconnect();
	}

	// Load file list from server
	private void loadFileListFromServer() {
		try {
			URL url = new URL(IP.SERVER_IP + "/files?username=" + username);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				try (InputStream is = connection.getInputStream(); Scanner scanner = new Scanner(is)) {

					List<String> files = new ArrayList<>();
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine().trim();
						if (!line.isEmpty()) {
							files.add(line);
						}
					}
					listView.getItems().setAll(files);
				}
			} else {
				showAlert("Failed to load file list: HTTP " + responseCode);
			}

			connection.disconnect();
		} catch (IOException e) {
			showAlert("Failed to load file list: " + e.getMessage());
		}
	}

	// Delete file from server
	private void deleteFileFromServer(String fileName) throws IOException {
		String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
		String url = IP.SERVER_IP + "/delete?username=" + username + "&filename=" + encodedFileName;

		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestMethod("DELETE");

		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			showAlert("File deleted successfully!");
		} else {
			showAlert("File deletion failed with HTTP code: " + responseCode);
		}

		connection.disconnect();
	}

	private void sendLogoutRequest() {
		try {
			String url = IP.SERVER_IP + "/logout?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("POST");

			int responseCode = connection.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				showAlert("Logout failed: HTTP " + responseCode);
			}

			connection.disconnect();
		} catch (IOException ex) {
			showAlert("Logout request failed: " + ex.getMessage());
		}
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
