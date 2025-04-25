package hoyocon.bomberman;

import com.almasb.fxgl.entity.Entity;
import hoyocon.bomberman.EntitiesState.State;
import hoyocon.bomberman.Map.Map1;
import hoyocon.bomberman.Object.Player;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

import hoyocon.bomberman.Buff.Bomb;
import hoyocon.bomberman.Buff.Flame;
import hoyocon.bomberman.Buff.Speed;
import hoyocon.bomberman.Object.BuffEntity;
import hoyocon.bomberman.Buff.BuffGeneric;
import hoyocon.bomberman.Map.Map;

public class GameSceneBuilder {
    private static double savedX = 195;
    private static double savedY = 70;

    private static final double screenWidth = 1920;
    private static final double screenHeight = 1080;
    private static final double speed = 10;

    // Thêm các biến theo dõi trạng thái phím
    private static boolean isUpPressed = false;
    private static boolean isDownPressed = false;
    private static boolean isLeftPressed = false;
    private static boolean isRightPressed = false;

    // Quản lí buff.
    private static List<BuffEntity> buffEntities = new ArrayList<>();

    private static void addBuffToMap(Pane gamePane, BuffGeneric buff, double x, double y) {
        BuffEntity buffEntity = new BuffEntity(buff, x, y);
        buffEntities.add(buffEntity); // Lưu buff vào danh sách
        gamePane.getChildren().add(buffEntity.getImageView()); // Thêm hình ảnh buff vào gamePane
    }

    private static void checkPlayerBuffCollision(Player player, Pane gamePane) {
        List<BuffEntity> collectedBuffs = new ArrayList<>();

        for (BuffEntity buffEntity : buffEntities) {
            if (player.getBounds().intersects(buffEntity.getImageView().getBoundsInParent())) {
                // Áp dụng buff cho người chơi
                buffEntity.getBuff().apply(player);

                // Thêm vào danh sách các buff đã thu thập
                collectedBuffs.add(buffEntity);

                // Xóa hình ảnh buff khỏi gamePane
                gamePane.getChildren().remove(buffEntity.getImageView());
            }
        }

        // Xóa các buff đã thu thập khỏi danh sách buffEntities
        buffEntities.removeAll(collectedBuffs);
    }

    public static Scene buildNewGameScene() {
        // Reset về vị trí ban đầu
        savedX = 195;
        savedY = 70;
        return buildGameScene(savedX, savedY);
    }

    public static Scene buildContinueScene() {
        return buildGameScene(savedX, savedY);
    }

    private static Scene buildGameScene(double startX, double startY) {
        Pane gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: black;"); // Đổi màu nền

        // Tạo và hiển thị map trước khi tạo player
        Map gameMap = new Map(Map1.getMapData());
        gameMap.render();
        gamePane.getChildren().add(gameMap.getCanvas());

        // Làm sạch danh sách buff
        buffEntities.clear();

        // Tạo entity và thêm Player component
        Entity playerEntity = new Entity();
        Player playerComponent = new Player();
        playerEntity.addComponent(playerComponent);
        
        // Đặt vị trí ban đầu - điều chỉnh thành vị trí phù hợp trong map
        // Vị trí tile 1,1 (ô trống đầu tiên sau viền tường)
        startX = Map.TILE_SIZE + 8;  // Điều chỉnh vị trí trong ô (48 + 8)
        startY = Map.TILE_SIZE + 8;  // Điều chỉnh vị trí trong ô
        playerEntity.setPosition(startX, startY);
        
        // Thêm playerEntity vào gamePane
        gamePane.getChildren().add(playerEntity.getViewComponent().getParent());

        // Thêm các buff vào bản đồ
        addBuffToMap(gamePane, new Bomb(), 300, 300); // Buff Bomb tại vị trí (300, 300)
        addBuffToMap(gamePane, new Speed(), 500, 500); // Buff Speed tại vị trí (500, 500)
        addBuffToMap(gamePane, new Flame(), 700, 700); // Buff Flame tại vị trí (700, 700)

        Scene scene = new Scene(gamePane, screenWidth, screenHeight);

        // Ẩn con trỏ chuột khi vào game
        scene.setCursor(Cursor.NONE);

        // Focus
        gamePane.setOnMouseClicked(e -> gamePane.requestFocus());
        gamePane.requestFocus();

        // Sửa đổi AnimationTimer để xử lý va chạm với map
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Di chuyển dựa trên trạng thái phím
                double x = playerEntity.getX();
                double y = playerEntity.getY();
                double playerWidth = 40;  // Chiều rộng của player
                double playerHeight = 40; // Chiều cao của player
                boolean moved = false;
                
                // Kiểm tra va chạm với map
                if (isUpPressed) {
                    int nextRow = Map.pixelToTile(y - speed);
                    int currentCol = Map.pixelToTile(x + playerWidth/2);
                    if (nextRow >= 0 && gameMap.isWalkable(nextRow, currentCol)) {
                        playerComponent.moveUp(0.008);
                        moved = true;
                    }
                }
                if (isDownPressed) {
                    int nextRow = Map.pixelToTile(y + playerHeight + speed);
                    int currentCol = Map.pixelToTile(x + playerWidth/2);
                    if (nextRow < gameMap.height && gameMap.isWalkable(nextRow, currentCol)) {
                        playerComponent.moveDown(0.008);
                        moved = true;
                    }
                }
                if (isLeftPressed) {
                    int currentRow = Map.pixelToTile(y + playerHeight/2);
                    int nextCol = Map.pixelToTile(x - speed);
                    if (nextCol >= 0 && gameMap.isWalkable(currentRow, nextCol)) {
                        playerComponent.moveLeft(0.008);
                        moved = true;
                    }
                }
                if (isRightPressed) {
                    int currentRow = Map.pixelToTile(y + playerHeight/2);
                    int nextCol = Map.pixelToTile(x + playerWidth + speed);
                    if (nextCol < gameMap.width && gameMap.isWalkable(currentRow, nextCol)) {
                        playerComponent.moveRight(0.008);
                        moved = true;
                    }
                }
                if(!moved){
                    playerComponent.stop();
                }
                
                // Gọi onUpdate để cập nhật animation
                playerComponent.onUpdate(0.016);

                // Kiểm tra va chạm giữa người chơi và buff
                checkPlayerBuffCollision(playerComponent, gamePane);
            }
        };
        gameLoop.start();

        // Xử lý phím nhấn
        gamePane.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.W) {
                isUpPressed = true;
            } else if (event.getCode() == KeyCode.S) {
                isDownPressed = true;
            } else if (event.getCode() == KeyCode.A) {
                isLeftPressed = true;
            } else if (event.getCode() == KeyCode.D) {
                isRightPressed = true;
            } else if (event.getCode() == KeyCode.SPACE) {
                playerComponent.placeBomb();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                // Lưu vị trí hiện tại
                savedX = playerEntity.getX();
                savedY = playerEntity.getY();
                
                try {
                    // Dừng game loop khi thoát
                    gameLoop.stop();
                    
                    Parent menuRoot = FXMLLoader.load(GameSceneBuilder.class.getResource("/hoyocon/bomberman/Menu-view.fxml"));
                    Scene menuScene = new Scene(menuRoot, screenWidth, screenHeight);

                    // Hiện lại chuột khi quay về menu
                    menuScene.setCursor(Cursor.DEFAULT);

                    Stage stage = (Stage) scene.getWindow();
                    stage.setScene(menuScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        // Xử lý khi thả phím
        gamePane.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.W) {
                isUpPressed = false;
            } else if (event.getCode() == KeyCode.S) {
                isDownPressed = false; 
            } else if (event.getCode() == KeyCode.A) {
                isLeftPressed = false;
            } else if (event.getCode() == KeyCode.D) {
                isRightPressed = false;
            }
            
            if (!isUpPressed && !isDownPressed && !isLeftPressed && !isRightPressed) {
                playerComponent.stop();
            }
        });

        return scene;
    }
}