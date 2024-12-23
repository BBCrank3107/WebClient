package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javafx.scene.input.MouseEvent;
import ip.IP;

public class ManagerPage extends Application {

    private String email;
    private String username;
    private ImageView avatarImageView;
    private Label lblUsernameText;
    private VBox projectContainer;
    private int projectCount;
    private static final int PROJECTS_PER_HBOX = 3;

    public ManagerPage(String email) {
        this.email = email;
        this.projectCount = 0;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Manager");

        avatarImageView = createAvatarImageView();
        lblUsernameText = new Label("Loading username...");
        lblUsernameText.getStyleClass().add("username-label");

        Button btnCreate = new Button("Create Project");
        Button btnLogout = new Button("Logout");
        btnCreate.getStyleClass().add("create-button");
        btnLogout.getStyleClass().add("logout-button");

        projectContainer = new VBox(10);
        projectContainer.setPadding(new javafx.geometry.Insets(10));
        projectContainer.setSpacing(10);
        
        VBox projectWrapper = new VBox();
        VBox.setMargin(projectContainer, new javafx.geometry.Insets(30, 0, 0, 0));
        projectWrapper.getChildren().add(projectContainer);

        btnLogout.setOnAction(e -> {
            sendLogoutRequest();
            new LoginPage().start(primaryStage);
        });

        btnCreate.setOnAction(e -> showCreateProjectDialog());

        VBox leftContainer = new VBox(10, avatarImageView, lblUsernameText);
        leftContainer.getStyleClass().add("left-container");

        BorderPane layout = new BorderPane();
        layout.setLeft(leftContainer);
        layout.setCenter(projectWrapper);
        layout.setBottom(new HBox(10, btnCreate, btnLogout));

        layout.setStyle("-fx-padding: 20; -fx-background-color: #ffffff;");

        Scene scene = new Scene(layout, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();

        getUserNameFromServer();
        scene.getStylesheets().add(getClass().getResource("/css/ManagerPage.css").toExternalForm());
    }

    private ImageView createAvatarImageView() {
        ImageView imageView = new ImageView();
        imageView.setImage(new Image("file:images/avatar.png"));
        imageView.setFitWidth(200);
        imageView.setFitHeight(200);
        Circle clip = new Circle(100, 100, 100);
        imageView.setClip(clip);
        imageView.getStyleClass().add("avatar-image");
        return imageView;
    }

    private void showCreateProjectDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Project");
        dialog.setHeaderText("Enter project name:");
        dialog.setContentText("Project Name:");
        dialog.showAndWait().ifPresent(this::createProjectOnServer);
    }

    private void createProjectOnServer(String projectName) {
        try {
            String url = IP.SERVER_IP + "/createProject";
            String params = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                            "&projectName=" + URLEncoder.encode(projectName, StandardCharsets.UTF_8) + 
                            "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = params.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                showAlert("Project created successfully!");
                getProjectListFromServer();
            } else {
                showAlert("Failed to create project: HTTP " + connection.getResponseCode());
            }

            connection.disconnect();
        } catch (IOException ex) {
            showAlert("Request failed: " + ex.getMessage());
        }
    }

    private void sendLogoutRequest() {
        try {
            String url = IP.SERVER_IP + "/logout?username=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                showAlert("Logout failed: HTTP " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (IOException ex) {
            showAlert("Logout request failed: " + ex.getMessage());
        }
    }

    private void getUserNameFromServer() {
        try {
            String url = IP.SERVER_IP + "/getUserName?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                username = in.readLine();
                in.close();
                lblUsernameText.setText(username);
                getProjectListFromServer();
            } else {
                showAlert("Failed to fetch username: HTTP " + connection.getResponseCode());
                lblUsernameText.setText(email);
            }
            connection.disconnect();
        } catch (IOException ex) {
            showAlert("Request failed: " + ex.getMessage());
            lblUsernameText.setText(email);
        }
    }

    private void getProjectListFromServer() {
        try {
            String url = IP.SERVER_IP + "/listProjects?username=" + URLEncoder.encode(username, StandardCharsets.UTF_8);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = in.readLine();
                in.close();

                projectContainer.getChildren().clear();
                projectCount = 0;

                if (response == null || response.isEmpty()) {
                    Label noProjectsLabel = new Label("No projects available.");
                    noProjectsLabel.getStyleClass().add("no-projects-label");
                    projectContainer.getChildren().add(noProjectsLabel);
                    return;
                }

                String[] projects = response.split(",");
                HBox currentHBox = new HBox(10);
                currentHBox.setSpacing(10);

                for (String project : projects) {
                    Label projectLabel = new Label(project);
                    projectLabel.getStyleClass().add("project-card");

                    projectLabel.setOnMouseClicked((MouseEvent event) -> {
                        ProjectPage projectPage = new ProjectPage(email, username, project);
                        try {
                            projectPage.start((Stage) projectLabel.getScene().getWindow());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

                    currentHBox.getChildren().add(projectLabel);
                    projectCount++;

                    if (projectCount % PROJECTS_PER_HBOX == 0) {
                        projectContainer.getChildren().add(currentHBox);
                        currentHBox = new HBox(10);
                        currentHBox.setSpacing(10);
                    }
                }

                if (!currentHBox.getChildren().isEmpty()) {
                    projectContainer.getChildren().add(currentHBox);
                }
            } else {
                showAlert("Failed to fetch projects: HTTP " + connection.getResponseCode());
            }
            connection.disconnect();
        } catch (IOException ex) {
            showAlert("Request failed: " + ex.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
