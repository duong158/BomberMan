package hoyocon.bomberman;

import com.fasterxml.jackson.databind.ObjectMapper;
import hoyocon.bomberman.api.entity.LoginRequest;
import hoyocon.bomberman.api.entity.LoginResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Controller xử lý đăng nhập thông qua REST API Spring Boot.
 */
public class LoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;

    private static final String BASE_URL = "http://localhost:8080/api/auth";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LoginController() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String user = txtUsername.getText().trim();
        String pass = txtPassword.getText().trim();

        try {
            // Tạo request object và chuyển thành JSON
            LoginRequest reqObj = new LoginRequest();
            reqObj.setUsername(user);
            reqObj.setPassword(pass);
            String requestBody = objectMapper.writeValueAsString(reqObj);

            // Gửi POST đến /login
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Đăng nhập thành công
                LoginResponse respObj = objectMapper.readValue(response.body(), LoginResponse.class);
                // Tải scene CoopLobby
                Parent root = FXMLLoader.load(getClass().getResource("/FXML/CoopLobby.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } else if (response.statusCode() == 401) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Sai tên đăng nhập hoặc mật khẩu");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Server trả về mã: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Exception", e.getMessage());
        }
    }

    @FXML
    private void openRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/FXML/Register.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Không thể mở màn hình Register");
        }
    }

    @FXML
    private void handleGuest(ActionEvent event) {

    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
