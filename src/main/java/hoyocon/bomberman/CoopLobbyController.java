package hoyocon.bomberman;

import com.esotericsoftware.kryonet.Client;
import hoyocon.bomberman.network.Network;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.event.ActionEvent;

import java.io.IOException;

public class CoopLobbyController {
    @FXML private ListView<String> friendList;

    private Client client;

    @FXML
    public void initialize() {
        friendList.getItems().addAll("Alice", "Bob", "Charlie");

        client = new Client();
        Network.register(client.getKryo()); // đăng ký class cần thiết cho Kryo

        client.start();
        try {
            client.connect(5000, "localhost", 54555, 54777); // địa chỉ & cổng server
        } catch (IOException e) {
            showError("Không thể kết nối server: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendRequest(ActionEvent event) {
        String target = friendList.getSelectionModel().getSelectedItem();
        if (target != null) {
            Network.InviteRequest request = new Network.InviteRequest();
            request.targetName = target;
            request.fromName = "Bạn"; // Hoặc lấy tên người chơi thực tế

            client.sendTCP(request);

            showInfo("Đã gửi lời mời đến " + target);
        } else {
            showWarning("Vui lòng chọn một bạn bè");
        }
    }

    @FXML
    private void handleStartGame(ActionEvent event) {
        showInfo("Bắt đầu game với bạn bè!");
    }

    private void showInfo(String msg) {
        showAlert(Alert.AlertType.INFORMATION, msg);
    }

    private void showWarning(String msg) {
        showAlert(Alert.AlertType.WARNING, msg);
    }

    private void showError(String msg) {
        showAlert(Alert.AlertType.ERROR, msg);
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
}
