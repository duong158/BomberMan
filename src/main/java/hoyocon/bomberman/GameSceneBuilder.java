package hoyocon.bomberman;

import hoyocon.bomberman.Object.Player;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import com.almasb.fxgl.entity.Entity;

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
        gamePane.setStyle("-fx-background-color: lightgray;");

        // Tạo entity và thêm Player component
        Entity playerEntity = new Entity();
        Player playerComponent = new Player();
        playerEntity.addComponent(playerComponent);
        
        // Đặt vị trí ban đầu
        playerEntity.setPosition(startX, startY);
        
        // Thêm playerEntity vào gamePane
        gamePane.getChildren().add(playerEntity.getViewComponent().getParent());

        Scene scene = new Scene(gamePane, screenWidth, screenHeight);

        // Ẩn con trỏ chuột khi vào game
        scene.setCursor(Cursor.NONE);

        // Focus
        gamePane.setOnMouseClicked(e -> gamePane.requestFocus());
        gamePane.requestFocus();

        // Chỉnh sửa AnimationTimer để xử lý di chuyển trong game loop
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Di chuyển dựa trên trạng thái phím
                double x = playerEntity.getX();
                double y = playerEntity.getY();
                boolean moved = false;
                
                if (isUpPressed && y - speed >= 0) {
                    playerComponent.moveUp(0.008);
                    moved = true;
                } if (isDownPressed && y + speed + 40 <= screenHeight) {
                    playerComponent.moveDown(0.008);
                    moved = true;
                } if (isLeftPressed && x - speed >= 0) {
                    playerComponent.moveLeft(0.008);
                    moved = true;
                } if (isRightPressed && x + speed + 40 <= screenWidth) {
                    playerComponent.moveRight(0.008);
                    moved = true;
                } if(!moved){
                    playerComponent.stop();
                }
                
                // Gọi onUpdate để cập nhật animation
                playerComponent.onUpdate(0.016);
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