package hoyocon.bomberman.manager;

import com.almasb.fxgl.entity.Entity;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import hoyocon.bomberman.Map.GMap;
import hoyocon.bomberman.Object.Player;
import hoyocon.bomberman.network.GameClient;
import hoyocon.bomberman.network.Network;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;

public class NetworkGameManager {
    private static NetworkGameManager instance;

    private GameClient client;
    private String playerName;
    private Entity localPlayer;
    private Map<String, Entity> remotePlayers = new HashMap<>();
    private Pane gamePane;
    private GMap gameMap;

    // Singleton pattern
    public static NetworkGameManager getInstance() {
        if (instance == null) {
            instance = new NetworkGameManager();
        }
        return instance;
    }

    private NetworkGameManager() {}

    public void initialize(GameClient client, String playerName, Entity localPlayer, Pane gamePane, GMap gameMap) {
        this.client = client;
        this.playerName = playerName;
        this.localPlayer = localPlayer;
        this.gamePane = gamePane;
        this.gameMap = gameMap;

        setupListeners();
    }

    private void setupListeners() {
        client.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                Platform.runLater(() -> {
                    if (object instanceof Network.PlayerMove) {
                        handlePlayerMove((Network.PlayerMove) object);
                    }
                    else if (object instanceof Network.PlaceBomb) {
                        handlePlaceBomb((Network.PlaceBomb) object);
                    }
                });
            }
        });
    }

    public void sendPlayerMove(int direction) {
        if (client != null && client.isConnected()) {
            Network.PlayerMove move = new Network.PlayerMove();
            move.playerName = playerName;
            move.direction = direction;
            client.getClient().sendTCP(move);
        }
    }

    public void sendPlaceBomb(int row, int col) {
        if (client != null && client.isConnected()) {
            Network.PlaceBomb bomb = new Network.PlaceBomb();
            bomb.playerName = playerName;
            bomb.row = row;
            bomb.col = col;
            client.getClient().sendTCP(bomb);
        }
    }

    private void handlePlayerMove(Network.PlayerMove move) {
        // Xử lý khi nhận được thông tin di chuyển từ người chơi khác
        if (!move.playerName.equals(playerName)) {
            Entity remotePlayer = getOrCreateRemotePlayer(move.playerName);
            Player playerComponent = remotePlayer.getComponent(Player.class);

            switch (move.direction) {
                case 0: // UP
                    playerComponent.moveUp(1.0/60.0);
                    break;
                case 1: // RIGHT
                    playerComponent.moveRight(1.0/60.0);
                    break;
                case 2: // DOWN
                    playerComponent.moveDown(1.0/60.0);
                    break;
                case 3: // LEFT
                    playerComponent.moveLeft(1.0/60.0);
                    break;
            }
        }
    }

    private void handlePlaceBomb(Network.PlaceBomb bomb) {
        // Xử lý khi nhận được thông tin đặt bom từ người chơi khác
        if (!bomb.playerName.equals(playerName)) {
            Entity remotePlayer = getOrCreateRemotePlayer(bomb.playerName);
            Player playerComponent = remotePlayer.getComponent(Player.class);
            playerComponent.placeBomb(gamePane);
        }
    }

    private Entity getOrCreateRemotePlayer(String name) {
        // Tạo người chơi từ xa nếu chưa tồn tại
        if (!remotePlayers.containsKey(name)) {
            Entity playerEntity = new Entity();
            Player playerComponent = new Player();
            playerComponent.setGameMap(gameMap);
            playerEntity.addComponent(playerComponent);

            // Đặt người chơi ở góc đối diện
            playerEntity.setPosition(
                gameMap.width * GMap.TILE_SIZE - GMap.TILE_SIZE * 2,
                gameMap.height * GMap.TILE_SIZE - GMap.TILE_SIZE * 2
            );

            // Thêm vào scene
            gamePane.getChildren().add(playerEntity.getViewComponent().getParent());

            remotePlayers.put(name, playerEntity);
        }

        return remotePlayers.get(name);
    }
}