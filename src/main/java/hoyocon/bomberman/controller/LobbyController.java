package hoyocon.bomberman.controller;

import hoyocon.bomberman.GameSceneBuilder;
import hoyocon.bomberman.Main;
import hoyocon.bomberman.network.GameClient;
import hoyocon.bomberman.network.GameServer;
import hoyocon.bomberman.network.Network;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.util.HashMap;
import java.util.Map;

public class LobbyController {
    @FXML private TextField hostNameField;
    @FXML private Button hostGameBtn;
    @FXML private Label hostIpLabel;
    @FXML private TextField clientNameField;
    @FXML private TextField serverIPField;
    @FXML private Button joinGameBtn;
    @FXML private ListView<String> playerListView;
    @FXML private Button startGameBtn;
    @FXML private Label statusLabel;

    private GameServer server;
    private GameClient client;
    private ObservableList<String> playerList = FXCollections.observableArrayList();
    private boolean isHost = false;

    // Dữ liệu game để truyền qua mạng
    private static Map<String, Object> gameData = new HashMap<>();

    @FXML
    public void initialize() {
        playerListView.setItems(playerList);
        startGameBtn.setDisable(true);

        // Hiển thị IP của máy này để người chơi khác kết nối vào
        try {
            String localIP = java.net.InetAddress.getLocalHost().getHostAddress();
            hostIpLabel.setText("Địa chỉ IP của bạn: " + localIP);
        } catch (Exception e) {
            hostIpLabel.setText("Không thể lấy địa chỉ IP");
        }
    }

    @FXML
    private void onHostGame() {
        String name = hostNameField.getText().trim();

        if (name.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tên của bạn!");
            return;
        }

        if (server == null) {
            // Khởi động server
            server = new GameServer();
            server.start();

            // Cập nhật UI
            hostGameBtn.setText("Dừng máy chủ");
            statusLabel.setText("Đã khởi động máy chủ. Đang chờ người chơi khác...");
            hostNameField.setDisable(true);

            // Kết nối đến server với tư cách host
            connectToServer("localhost", name);
            isHost = true;

            // Chỉ host mới được nhấn Start
            if (playerList.size() >= 2) {
                startGameBtn.setDisable(false);
            }
        } else {
            // Dừng server
            if (client != null) {
                client.close();
                client = null;
            }
            server.stop();
            server = null;

            // Cập nhật UI
            hostGameBtn.setText("Tạo máy chủ");
            statusLabel.setText("Máy chủ đã dừng");
            hostNameField.setDisable(false);
            playerList.clear();
            startGameBtn.setDisable(true);
            isHost = false;
        }
    }

    @FXML
    private void onJoinGame() {
        String name = clientNameField.getText().trim();
        String ip = serverIPField.getText().trim();

        if (name.isEmpty() || ip.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tên của bạn và địa chỉ IP máy chủ!");
            return;
        }

        if (client == null) {
            // Kết nối đến server
            connectToServer(ip, name);

            // Cập nhật UI
            joinGameBtn.setText("Ngắt kết nối");
            clientNameField.setDisable(true);
            serverIPField.setDisable(true);
        } else {
            // Ngắt kết nối
            client.close();
            client = null;

            // Cập nhật UI
            joinGameBtn.setText("Tham gia");
            clientNameField.setDisable(false);
            serverIPField.setDisable(false);
            playerList.clear();
        }
    }

    private void connectToServer(String ip, String name) {
        try {
            client = new GameClient();
            boolean connected = client.connect(ip, name);

            if (connected) {
                // Thiết lập listener
                client.getClient().addListener(new Listener() {
                    @Override
                    public void received(Connection connection, Object object) {
                        Platform.runLater(() -> {
                            // Khi server phản hồi join request
                            if (object instanceof Network.JoinResponse) {
                                Network.JoinResponse response = (Network.JoinResponse) object;
                                statusLabel.setText(response.message);

                                // Thêm người chơi vào danh sách
                                if (response.accepted && !playerList.contains(name)) {
                                    playerList.add(name);
                                }
                            }
                            // Khi có người chơi mới tham gia
                            else if (object instanceof Network.PlayerJoined) {
                                Network.PlayerJoined joined = (Network.PlayerJoined) object;
                                if (!playerList.contains(joined.playerName)) {
                                    playerList.add(joined.playerName);
                                    statusLabel.setText("Người chơi " + joined.playerName + " đã tham gia!");

                                    // Cập nhật nút Start
                                    if (isHost && playerList.size() >= 2) {
                                        startGameBtn.setDisable(false);
                                    }
                                }
                            }
                            // Khi server gửi tín hiệu bắt đầu game
                            else if (object instanceof Network.StartGameSignal) {
                                startGame();
                            }
                        });
                    }

                    @Override
                    public void disconnected(Connection connection) {
                        Platform.runLater(() -> {
                            statusLabel.setText("Mất kết nối với máy chủ!");
                            if (client != null) {
                                client.close();
                                client = null;
                            }

                            // Reset UI
                            if (isHost) {
                                hostGameBtn.setText("Tạo máy chủ");
                                hostNameField.setDisable(false);
                            } else {
                                joinGameBtn.setText("Tham gia");
                                clientNameField.setDisable(false);
                                serverIPField.setDisable(false);
                            }
                            playerList.clear();
                            startGameBtn.setDisable(true);
                        });
                    }
                });

                // Gửi yêu cầu tham gia
                client.sendJoinRequest(name);
            } else {
                statusLabel.setText("Không thể kết nối đến máy chủ!");
            }
        } catch (Exception e) {
            showAlert("Lỗi kết nối", e.getMessage());
        }
    }

    @FXML
    private void onStartGame() {
        if (isHost && client != null) {
            client.sendStartGameRequest();
        }
    }

    private void startGame() {
        try {
            // Lưu thông tin game
            gameData.put("client", client);
            gameData.put("isHost", isHost);
            gameData.put("playerName", isHost ? hostNameField.getText() : clientNameField.getText());

            // Chuyển sang màn hình game
            Scene gameScene = createMultiplayerGameScene();
            Main.mainStage.setScene(gameScene);
            Main.mainStage.setTitle("Bomberman - Multiplayer");
        } catch (Exception e) {
            showAlert("Lỗi", "Không thể bắt đầu game: " + e.getMessage());
        }
    }

    private Scene createMultiplayerGameScene() {
        // Đảm bảo client đã được khởi tạo
        if (client == null || !client.isConnected()) {
            System.err.println("Lỗi: Client chưa kết nối");
            showAlert("Lỗi kết nối", "Không thể kết nối đến máy chủ.");
            return null;
        }

        // Lưu thông tin game vào gameData để có thể truy cập từ các lớp khác
        gameData.put("client", client);
        gameData.put("playerName", isHost ? hostNameField.getText() : clientNameField.getText());
        gameData.put("isHost", isHost);

        // Gọi phương thức tạo game scene từ GameSceneBuilder
        return GameSceneBuilder.buildMultiplayerGameScene(client);
    }

    // Phương thức để truy cập dữ liệu game từ các controller khác
    public static Map<String, Object> getGameData() {
        return gameData;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}