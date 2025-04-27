package hoyocon.bomberman;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
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
import hoyocon.bomberman.Map.GMap;

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
        gamePane.setFocusTraversable(true);
        gamePane.setStyle("-fx-background-color: black;"); // Đổi màu nền

        // Tạo và hiển thị map trước khi tạo player
        GMap gameGMap = new GMap(Map1.getMapData());
        gameGMap.render();
        gamePane.getChildren().add(gameGMap.getCanvas());

        // Làm sạch danh sách buff
        buffEntities.clear();

        // Tạo entity và thêm Player component
        Entity playerEntity = new Entity();
        Player playerComponent = new Player();
        playerEntity.addComponent(playerComponent);

        // Set the game map for collision detection
        playerComponent.setGameMap(gameGMap);
        
        // Đặt vị trí ban đầu - điều chỉnh thành vị trí phù hợp trong map
        // Vị trí tile 1,1 (ô trống đầu tiên sau viền tường)
        startX = GMap.TILE_SIZE;  // Điều chỉnh vị trí trong ô (48 + 8)
        startY = GMap.TILE_SIZE;  // Điều chỉnh vị trí trong ô
        playerEntity.setPosition(startX, startY);
        
        // Thêm playerEntity vào gamePane
        gamePane.getChildren().add(playerEntity.getViewComponent().getParent());

        // Thêm các buff vào bản đồ (giữ nguyên code này)
        addBuffToMap(gamePane, new Bomb(), 300, 300);
        addBuffToMap(gamePane, new Speed(), 500, 500);
        addBuffToMap(gamePane, new Flame(), 700, 700);

        Scene scene = new Scene(gamePane, screenWidth, screenHeight);

        // Ẩn con trỏ chuột khi vào game
        scene.setCursor(Cursor.NONE);

        // Focus
        gamePane.setOnMouseClicked(e -> gamePane.requestFocus());
        gamePane.requestFocus();

        // Sửa đổi AnimationTimer để sử dụng phương thức từ Player
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean moved = false;

                if (isUpPressed) {
                    moved = playerComponent.moveUp(0.016);
                }
                else if (isDownPressed) {
                    moved = playerComponent.moveDown(0.016);
                }
                else if (isLeftPressed) {
                    moved = playerComponent.moveLeft(0.016);
                }
                else if (isRightPressed) {
                    moved = playerComponent.moveRight(0.016);
                }
                
                if(!moved){
                    playerComponent.stop();
                }

                playerComponent.onUpdate(0.5);

                // Use player's method to check buff collisions
                playerComponent.checkBuffCollision(buffEntities, gamePane);
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
                playerComponent.placeBomb(gamePane);
                System.out.println("Key pressed: " + event.getCode());
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