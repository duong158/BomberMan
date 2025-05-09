package hoyocon.bomberman;

import com.esotericsoftware.kryonet.Client;
import hoyocon.bomberman.network.Network;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class CoopLobbyController {
    @FXML private ListView<String> friendList;
    @FXML private TextField ipField;
    private boolean connected = false;

    private Client client;

    @FXML
    public void initialize() {
        friendList.getItems().addAll("Alice", "Bob", "Charlie");
        // Không tự động kết nối khi vào lobby nữa
    }

    @FXML
    private void handleConnect(ActionEvent event) {
        if (connected) {
            showInfo("Đã kết nối server!");
            return;
        }
        String ip = ipField.getText();
        if (ip == null || ip.isEmpty()) ip = "localhost";
        client = new Client();
        Network.register(client.getKryo());
        client.start();
        try {
            client.connect(5000, ip, 7777, 7778);
            connected = true;
            showInfo("Kết nối server thành công!");

            // Đăng ký listener khi khởi tạo client
            client.addListener(new com.esotericsoftware.kryonet.Listener() {
                public void received(com.esotericsoftware.kryonet.Connection connection, Object object) {
                    if (object instanceof hoyocon.bomberman.network.Network.StartGameSignal) {
                        Platform.runLater(() -> {
                            Scene coopGameScene = hoyocon.bomberman.GameSceneBuilder.buildNewGameScene();
                            Stage stage = (Stage) friendList.getScene().getWindow();
                            stage.setScene(coopGameScene);
                            stage.setTitle("Bomberman Co-op");
                            coopGameScene.getRoot().requestFocus();
                        });
                    }
                }
            });
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
        if (client != null && connected) {
            client.sendTCP(new hoyocon.bomberman.network.Network.StartGameRequest());
        } else {
            showError("Bạn chưa kết nối server!");
        }
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
