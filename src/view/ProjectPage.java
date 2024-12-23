package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ip.IP;

public class ProjectPage extends Application {
    
    private String email;
    private String username;
    private String projectName;

    public ProjectPage(String email, String username, String projectName) {
        this.email = email;
        this.username = username;
        this.projectName = projectName;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Project Page");

        Label lblProjectName = new Label("Project: " + projectName);
        Button btnBack = new Button("Back");
        btnBack.getStyleClass().add("btnBack");
        Button btnAddCode = new Button("Add Code");
        Button btnDeleteFile = new Button("Delete file");
        btnDeleteFile.getStyleClass().add("btnDelete");
        Button btnDeleteProject = new Button("Delete Project");
        btnDeleteProject.getStyleClass().add("btnDelete");
        ListView<String> fileList = new ListView<>();
        
        btnBack.setOnAction(e -> {
            new ManagerPage(email).start(primaryStage);
        });

        btnAddCode.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                try {
                    uploadFileToServer(selectedFile, username, projectName, fileList);
                } catch (IOException ex) {
                    showAlert("File upload failed: " + ex.getMessage());
                }
            }
        });

        btnDeleteFile.setOnAction(e -> {
            String selectedFile = fileList.getSelectionModel().getSelectedItem();
            if (selectedFile != null) {
                String fileName = selectedFile.split("\\s+")[0];  

                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("Do you want to delete the file: " + fileName + "?");
                
                confirmationAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        deleteFileFromServer(fileName, username, projectName, fileList);
                    }
                });
            } else {
                showAlert("Please select a file to delete.");
            }
        });

        btnDeleteProject.setOnAction(e -> {
            // Hiển thị hộp thoại xác nhận
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirmation");
            confirmationAlert.setHeaderText(null);
            confirmationAlert.setContentText("Do you want to delete this project?");
            
            // Chờ người dùng chọn
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    deleteProjectFromServer(username, projectName, primaryStage);
                }
            });
        });

        VBox layout = new VBox(10, lblProjectName, fileList, btnAddCode, btnDeleteFile, btnDeleteProject, btnBack);
        layout.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");

        getFileListFromServer(fileList);

        Scene scene = new Scene(layout, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        scene.getStylesheets().add(getClass().getResource("/css/ProjectPage.css").toExternalForm());
    }

    private void uploadFileToServer(File file, String username, String projectName, ListView<String> fileList) throws IOException {
        String fileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8);
        String checkUrl = IP.SERVER_IP + "/checkFileExistence?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&project=" + URLEncoder.encode(projectName, StandardCharsets.UTF_8)
                + "&filename=" + fileName;

        HttpURLConnection checkConnection = (HttpURLConnection) new URL(checkUrl).openConnection();
        checkConnection.setRequestMethod("GET");

        int checkResponseCode = checkConnection.getResponseCode();
        if (checkResponseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(checkConnection.getInputStream()));
            String response = in.readLine();
            in.close();

            if ("exists".equals(response)) {
                // Hiển thị hộp thoại xác nhận
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Confirmation");
                confirmationAlert.setHeaderText(null);
                confirmationAlert.setContentText("File already exists. Do you want to replace it?");
                
                confirmationAlert.showAndWait().ifPresent(responseBtn -> {
                    if (responseBtn == ButtonType.OK) {
                        try {
                            uploadFileWithConfirmation(file, username, projectName, fileList);
                        } catch (IOException e) {
                            showAlert("File upload failed: " + e.getMessage());
                        }
                    }
                });
            } else {
                uploadFileWithConfirmation(file, username, projectName, fileList);
            }
        } else {
            showAlert("Failed to check file existence: HTTP " + checkResponseCode);
        }

        checkConnection.disconnect();
    }

    private void uploadFileWithConfirmation(File file, String username, String projectName, ListView<String> fileList) throws IOException {
        String fileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8);
        long fileSize = file.length();
        String url = IP.SERVER_IP + "/upload?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&project=" + URLEncoder.encode(projectName, StandardCharsets.UTF_8)
                + "&filename=" + fileName
                + "&fileSize=" + fileSize
                + "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);

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
            getFileListFromServer(fileList);
        } else {
            showAlert("File upload failed with HTTP code: " + responseCode);
        }

        connection.disconnect();
    }

    private void deleteFileFromServer(String fileName, String username, String projectName, ListView<String> fileList) {
        try {
            String url = IP.SERVER_IP + "/deleteFile?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                    "&projectName=" + URLEncoder.encode(projectName, StandardCharsets.UTF_8) +
                    "&fileName=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + 
                    "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
            
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                showAlert("File deleted successfully!");
                getFileListFromServer(fileList);
            } else {
                showAlert("Failed to delete file: HTTP " + responseCode);
            }

            connection.disconnect();
        } catch (IOException ex) {
            showAlert("Delete request failed: " + ex.getMessage());
        }
    }

    private void deleteProjectFromServer(String username, String projectName, Stage primaryStage) {
        try {
            String url = IP.SERVER_IP + "/deleteProject?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                    "&projectName=" + URLEncoder.encode(projectName, StandardCharsets.UTF_8) +
                    "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
            
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("DELETE");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                showAlert("Project deleted successfully!");
                primaryStage.close();
                new ManagerPage(email).start(new Stage());
            } else {
                showAlert("Failed to delete project: HTTP " + responseCode);
            }

            connection.disconnect();
        } catch (IOException ex) {
            showAlert("Delete project request failed: " + ex.getMessage());
        }
    }

    private void getFileListFromServer(ListView<String> fileList) {
        try {
            String url = IP.SERVER_IP + "/listFilesInProject?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&projectName=" + URLEncoder.encode(projectName, StandardCharsets.UTF_8);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                if (response.length() > 0) {
                    String responseBody = response.toString();

                    responseBody = responseBody.substring(1, responseBody.length() - 1).trim();

                    String[] files = responseBody.split("},\\{");

                    fileList.getItems().clear();
                    
                    for (String file : files) {
                        String fileName = extractValue(file, "fileName");
                        String fileSize = extractValue(file, "fileSize");
                        String elapsedTime = extractValue(file, "elapsedTime");

                        // Định dạng thông tin file
                        String fileInfo = String.format("%-30s %-15s %20s", fileName, fileSize, elapsedTime);
                        fileList.getItems().add(fileInfo);
                    }
                } else {
                    fileList.getItems().clear();
                    showAlert("No files found in this project.");
                }
            } else {
                showAlert("Failed to fetch files: HTTP " + responseCode);
            }

            connection.disconnect();
        } catch (IOException ex) {
            showAlert("Request failed: " + ex.getMessage());
        }
    }

    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":\"([^\"]+)\"";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
