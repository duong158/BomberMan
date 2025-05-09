package hoyocon.bomberman;

import com.fasterxml.jackson.databind.ObjectMapper;
import hoyocon.bomberman.api.entity.RegisterRequest;
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
 * Controller xử lý đăng ký thông qua REST API Spring Boot.
 */
public class RegisterController {
    @FXML private TextField txtNewUsername;
    @FXML private PasswordField txtNewPassword;

    private static final String BASE_URL = "http://localhost:8080/api/auth/register";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RegisterController() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String user = txtNewUsername.getText().trim();
        String pass = txtNewPassword.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Registration Error", "Username và password không được để trống");
            return;
        }

        try {
            // Tạo request object
            RegisterRequest reqObj = new RegisterRequest(user, pass);
            String requestBody = objectMapper.writeValueAsString(reqObj);

            // Gửi POST đến /register
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Đăng ký thành công! Vui lòng đăng nhập.");
                // Quay lại Login
                Parent root = FXMLLoader.load(getClass().getResource("/FXML/Login.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } else if (response.statusCode() == 409) {
                showAlert(Alert.AlertType.ERROR, "Registration Failed", "Username đã tồn tại");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Server trả về mã: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Exception", e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void openLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/FXML/Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
