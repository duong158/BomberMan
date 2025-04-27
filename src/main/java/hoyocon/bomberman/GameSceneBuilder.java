package hoyocon.bomberman;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import hoyocon.bomberman.Map.Map1;
import hoyocon.bomberman.Object.Player;
import hoyocon.bomberman.Object.EnemyGroup.Balloon;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Group;
import hoyocon.bomberman.Camera.Camera;

import hoyocon.bomberman.Buff.Bomb;
import hoyocon.bomberman.Buff.Flame;
import hoyocon.bomberman.Buff.Speed;
import hoyocon.bomberman.Object.BuffEntity;
import hoyocon.bomberman.Buff.BuffGeneric;
import hoyocon.bomberman.Map.GMap;

import java.util.ArrayList;
import java.util.List;

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
    
    // List to manage balloons
    private static List<Entity> balloonEntities = new ArrayList<>();

    private static void addBuffToMap(Pane gamePane, BuffGeneric buff, double x, double y) {
        BuffEntity buffEntity = new BuffEntity(buff, x, y);
        buffEntities.add(buffEntity); // Lưu buff vào danh sách
        gamePane.getChildren().add(buffEntity.getImageView()); // Thêm hình ảnh buff vào gamePane
    }
    
    // Method to spawn a balloon at the given map coordinates with exception handling
    private static void spawnBalloon(Pane gamePane, GMap gameGMap, int row, int col) {
        try {
            double x = col * GMap.TILE_SIZE;
            double y = row * GMap.TILE_SIZE;

            System.out.println("Attempting to spawn balloon at: row=" + row + ", col=" + col);

            // Create entity without using FXGL entity factory
            Entity balloonEntity = new Entity();

            // Create balloon component with tile coordinates
            Balloon balloonComponent = new Balloon(col, row);

            // Set gameMap reference for collision detection
            balloonComponent.setGameMap(gameGMap);

            // Add component to entity
            balloonEntity.addComponent(balloonComponent);

            // Set position in pixels
            balloonEntity.setPosition(x, y);

            // Add to our tracking list
            balloonEntities.add(balloonEntity);

            // Add to the game scene
            if (balloonEntity.getViewComponent() != null &&
                balloonEntity.getViewComponent().getParent() != null) {
                gamePane.getChildren().add(balloonEntity.getViewComponent().getParent());
                System.out.println("Balloon added to scene at x=" + x + ", y=" + y);
            } else {
                System.err.println("Warning: Balloon view component is null");
            }
        } catch (Exception e) {
            System.err.println("Error spawning balloon: " + e.getMessage());
            e.printStackTrace();
        }
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
        // Container chính cho toàn bộ scene
        Pane gamePane = new Pane();

        gamePane.setStyle("-fx-background-color: black;");

        // Tạo một Group để chứa thế giới game (camera sẽ di chuyển group này)
        Group gameWorld = new Group();
        gamePane.getChildren().add(gameWorld);

        gamePane.setFocusTraversable(true);


        // Tạo và hiển thị map
        GMap gameGMap = new GMap(Map1.getMapData(46, 23, 0.3f));
        gameGMap.render();
        gameWorld.getChildren().add(gameGMap.getCanvas());  // Thêm vào gameWorld thay vì gamePane

        // Làm sạch danh sách buff và balloons
        buffEntities.clear();
        balloonEntities.clear();

        try {
            // Spawn balloons at positions marked with 4 in the map
            List<int[]> balloonPositions = gameGMap.getBalloonPositions();
            System.out.println("Found " + balloonPositions.size() + " balloon positions in map");

            for (int[] position : balloonPositions) {
                spawnBalloon(gamePane, gameGMap, position[0], position[1]);
            }
        } catch (Exception e) {
            System.err.println("Error setting up balloons: " + e.getMessage());
            e.printStackTrace();
        }

        // Tạo entity và thêm Player component
        Entity playerEntity = new Entity();
        Player playerComponent = new Player();
        playerEntity.addComponent(playerComponent);

        // Set the game map for collision detection
        playerComponent.setGameMap(gameGMap);

        // Đặt vị trí ban đầu
        startX = GMap.TILE_SIZE;
        startY = GMap.TILE_SIZE;
        playerEntity.setPosition(startX, startY);

        // Thêm playerEntity vào gameWorld thay vì gamePane
        gameWorld.getChildren().add(playerEntity.getViewComponent().getParent());

        // Thêm các buff vào bản đồ
        addBuffToMap(gamePane, new Bomb(), 300, 300);
        addBuffToMap(gamePane, new Speed(), 500, 500);
        addBuffToMap(gamePane, new Flame(), 700, 700);

        // Tính kích thước thế giới game
        int worldWidth = gameGMap.width * (int)GMap.TILE_SIZE;
        int worldHeight = gameGMap.height * (int)GMap.TILE_SIZE;

        // Tạo camera theo dõi người chơi
        Camera camera = new Camera(
                gameWorld,
                playerEntity.getViewComponent().getParent(),
                (int)screenWidth,
                (int)screenHeight,
                worldWidth,
                worldHeight
        );

        Scene scene = new Scene(gamePane, screenWidth, screenHeight);

        // Ẩn con trỏ chuột khi vào game
        scene.setCursor(Cursor.NONE);

        // Focus
        gamePane.setOnMouseClicked(e -> gamePane.requestFocus());
        gamePane.requestFocus();

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean moved = false;

                if (isUpPressed) {
                    moved = playerComponent.moveUp(0.016);
                }
                if (isDownPressed) {
                    moved = playerComponent.moveDown(0.016);
                }
                if (isLeftPressed) {
                    moved = playerComponent.moveLeft(0.016);
                }
                if (isRightPressed) {
                    moved = playerComponent.moveRight(0.016);
                }

                if(!moved){
                    playerComponent.stop();
                }

                playerComponent.onUpdate(0.5);

                // Use player's method to check buff collisions
                playerComponent.checkBuffCollision(buffEntities, gamePane);
                // Update balloons
                for (Entity balloon : new ArrayList<>(balloonEntities)) {
                    if (balloon.getComponentOptional(Balloon.class).isPresent()) {
                        Balloon balloonComponent = balloon.getComponent(Balloon.class);
                        balloonComponent.onUpdate(1.0 / 60.0); // Assuming 60 FPS
                    }
                }
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