package hoyocon.bomberman.manager;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import hoyocon.bomberman.network.GameClient;
import hoyocon.bomberman.network.Network;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class NetworkGameManager {
    private static NetworkGameManager instance;

    private GameClient client;
    private String playerName;
    private Map<String, RemotePlayerView> remotePlayers = new HashMap<>();
    private Pane gamePane;

    // Singleton pattern
    public static NetworkGameManager getInstance() {
        if (instance == null) {
            instance = new NetworkGameManager();
        }
        return instance;
    }

    private NetworkGameManager() {}

    public void initialize(GameClient client, String playerName, Pane gamePane) {
        this.client = client;
        this.playerName = playerName;
        this.gamePane = gamePane;
        setupNetworkListener();
    }

    private void setupNetworkListener() {
        if (client != null) {
            client.getClient().addListener(new Listener() {
                @Override
                public void received(Connection connection, Object object) {
                    Platform.runLater(() -> {
                        // Xử lý các tin nhắn từ server
                        if (object instanceof Network.PlayerMove) {
                            handlePlayerMove((Network.PlayerMove) object);
                        }
                        else if (object instanceof Network.PlaceBomb) {
                            handlePlaceBomb((Network.PlaceBomb) object);
                        }
                        else if (object instanceof Network.BombExplode) {
                            handleBombExplode((Network.BombExplode) object);
                        }
                    });
                }
            });
        }
    }

    // Gửi tin nhắn di chuyển
    public void sendPlayerMove(int direction) {
        if (client != null && client.isConnected()) {
            Network.PlayerMove move = new Network.PlayerMove();
            move.playerName = playerName;
            move.direction = direction;
            client.getClient().sendTCP(move);
        }
    }

    // Gửi tin nhắn đặt bom
    public void sendPlaceBomb(int row, int col) {
        if (client != null && client.isConnected()) {
            Network.PlaceBomb bomb = new Network.PlaceBomb();
            bomb.playerName = playerName;
            bomb.row = row;
            bomb.col = col;
            client.getClient().sendTCP(bomb);
        }
    }

    // Xử lý khi nhận được tin nhắn di chuyển từ người chơi khác
    private void handlePlayerMove(Network.PlayerMove move) {
        // Chỉ xử lý tin nhắn từ người chơi khác
        if (!move.playerName.equals(playerName)) {
            RemotePlayerView player = getOrCreateRemotePlayer(move.playerName);

            // Cập nhật vị trí
            switch (move.direction) {
                case 0: // UP
                    player.moveUp();
                    break;
                case 1: // RIGHT
                    player.moveRight();
                    break;
                case 2: // DOWN
                    player.moveDown();
                    break;
                case 3: // LEFT
                    player.moveLeft();
                    break;
            }
        }
    }

    // Xử lý khi nhận được tin nhắn đặt bom
    private void handlePlaceBomb(Network.PlaceBomb bomb) {
        if (!bomb.playerName.equals(playerName)) {
            // Thêm code để tạo bom tại vị trí bomb.row, bomb.col
            System.out.println("Người chơi " + bomb.playerName + " đặt bom tại " + bomb.row + "," + bomb.col);
            // Gọi phương thức tạo bom trong game của bạn
        }
    }

    // Xử lý khi nhận được tin nhắn bom nổ
    private void handleBombExplode(Network.BombExplode explosion) {
        // Thêm code để tạo hiệu ứng nổ
        System.out.println("Bom nổ tại " + explosion.row + "," + explosion.col + " với phạm vi " + explosion.range);
        // Gọi phương thức tạo hiệu ứng nổ trong game của bạn
    }

    // Lấy hoặc tạo mới đối tượng hiển thị người chơi từ xa
    private RemotePlayerView getOrCreateRemotePlayer(String name) {
        if (!remotePlayers.containsKey(name)) {
            RemotePlayerView player = new RemotePlayerView(name, gamePane);
            remotePlayers.put(name, player);
            return player;
        }
        return remotePlayers.get(name);
    }

    // Lớp hiển thị người chơi từ xa
    private class RemotePlayerView {
        private String name;
        private Pane gamePane;
        private Rectangle playerRect;
        private double x = 100;
        private double y = 100;
        private double speed = 5.0;

        public RemotePlayerView(String name, Pane gamePane) {
            this.name = name;
            this.gamePane = gamePane;

            // Tạo hình chữ nhật đại diện cho người chơi từ xa
            playerRect = new Rectangle(32, 32);
            playerRect.setFill(Color.RED);
            playerRect.setTranslateX(x);
            playerRect.setTranslateY(y);

            // Thêm vào gamePane
            Platform.runLater(() -> gamePane.getChildren().add(playerRect));
        }

        public void moveUp() {
            y -= speed;
            updatePosition();
        }

        public void moveDown() {
            y += speed;
            updatePosition();
        }

        public void moveLeft() {
            x -= speed;
            updatePosition();
        }

        public void moveRight() {
            x += speed;
            updatePosition();
        }

        private void updatePosition() {
            playerRect.setTranslateX(x);
            playerRect.setTranslateY(y);
        }
    }
}