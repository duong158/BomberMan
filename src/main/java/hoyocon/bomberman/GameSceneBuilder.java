package hoyocon.bomberman;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import hoyocon.bomberman.Map.GMap;
import hoyocon.bomberman.Map.Map1;
import hoyocon.bomberman.Object.EnemyGroup.*;
import hoyocon.bomberman.Object.Player;
import hoyocon.bomberman.AI.PlayerAIController;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hoyocon.bomberman.Object.Player;
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
    
    // Thay thế các danh sách riêng biệt bằng một Map để quản lý tất cả các loại kẻ địch
    private static Map<Class<? extends Enemy>, List<Entity>> enemyEntities = new HashMap<>();
    // Add this new list to store all enemies in one place
    private static List<Enemy> allEnemyEntities = new ArrayList<>();

    public static void addBuffToMap(Pane gamePane, BuffGeneric buff, double x, double y) {
        BuffEntity buffEntity = new BuffEntity(buff, x, y);
        buffEntities.add(buffEntity); // Lưu buff vào danh sách
        gamePane.getChildren().add(buffEntity.getImageView()); // Thêm hình ảnh buff vào gamePane
    }
    
    // Method chung để spawn bất kỳ loại kẻ địch nào
    private static <T extends Enemy> void spawnEnemy(Pane gamePane, GMap gameGMap, int row, int col,
            Class<T> enemyClass, EnemyFactory<T> factory) {
        try {
            double x = col * GMap.TILE_SIZE;
            double y = row * GMap.TILE_SIZE;

            System.out.println("Attempting to spawn " + enemyClass.getSimpleName() + " at: row=" + row + ", col=" + col);
            Entity enemyEntity = new Entity();
            T enemyComponent = factory.create(col, row);
            enemyComponent.setGameMap(gameGMap);
            enemyEntity.addComponent(enemyComponent);
            enemyEntity.setPosition(x, y);

            // Thêm vào danh sách tương ứng
            enemyEntities.computeIfAbsent(enemyClass, k -> new ArrayList<>()).add(enemyEntity);
            // Also add to the flat list of all enemies
            allEnemyEntities.add(enemyComponent);

            if (enemyEntity.getViewComponent() != null &&
                enemyEntity.getViewComponent().getParent() != null) {
                gamePane.getChildren().add(enemyEntity.getViewComponent().getParent());
                System.out.println(enemyClass.getSimpleName() + " added to scene at x=" + x + ", y=" + y);
            } else {
                System.err.println("Warning: " + enemyClass.getSimpleName() + " view component is null");
            }
        } catch (Exception e) {
            System.err.println("Error spawning " + enemyClass.getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Interface functional để tạo kẻ địch
    @FunctionalInterface
    private interface EnemyFactory<T extends Enemy> {
        T create(int col, int row);
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
        GMap gameGMap = new GMap(Map1.getMapData(30, 23, 0.3f));
        gameGMap.render();
        gameWorld.getChildren().add(gameGMap.getCanvas());  // Thêm vào gameWorld thay vì gamePane

        // Làm sạch danh sách buff và balloons
        buffEntities.clear();
        enemyEntities.clear();

        try {
            // Spawn balloons
            List<int[]> balloonPositions = gameGMap.getESpawnPositions(GMap.BALLOON);
            System.out.println("Found " + balloonPositions.size() + " balloon positions in map");

            for (int[] position : balloonPositions) {
                spawnEnemy(gamePane, gameGMap, position[0], position[1], Balloon.class, Balloon::new);
            }

            // Spawn passes
            List<int[]> passPositions = gameGMap.getESpawnPositions(GMap.PASS);
            System.out.println("Found " + passPositions.size() + " pass positions in map");
            for (int[] position : passPositions) {
                spawnEnemy(gamePane, gameGMap, position[0], position[1], Pass.class, Pass::new);
            }

            // Spawn oneals (nếu có)
            List<int[]> onealPositions = gameGMap.getESpawnPositions(GMap.ONEAL);
            System.out.println("Found " + onealPositions.size() + " oneal positions in map");
            for (int[] position : onealPositions) {
                spawnEnemy(gamePane, gameGMap, position[0], position[1], Oneal.class, Oneal::new);
            }

            List<int[]> dahlPositions = gameGMap.getESpawnPositions(GMap.DAHL);
            System.out.println("Found " + onealPositions.size() + " oneal positions in map");
            for (int[] position : dahlPositions) {
                spawnEnemy(gamePane, gameGMap, position[0], position[1], Dahl.class, Dahl::new);
            }

            List<int[]> doriaPositions = gameGMap.getESpawnPositions(GMap.DORIA);
            System.out.println("Found " + doriaPositions.size() + " oneal positions in map");
            for (int[] position : doriaPositions) {
                spawnEnemy(gamePane, gameGMap, position[0], position[1], Doria.class, Doria::new);
            }

            // Có thể dễ dàng thêm các loại kẻ địch mới ở đây

        } catch (Exception e) {
            System.err.println("Error setting up enemies: " + e.getMessage());
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
        for (Map.Entry<Class<? extends Enemy>, List<Entity>> entry : enemyEntities.entrySet()) {
            Class<? extends Enemy> enemyClass = entry.getKey();
            List<Entity> entities = entry.getValue();
            
            // Kiểm tra xem đây có phải là Oneal hoặc lớp con của Oneal không
            if (Oneal.class.isAssignableFrom(enemyClass)) {
                for (Entity enemy : entities) {
                    if (enemy.getComponentOptional(enemyClass).isPresent()) {
                        Enemy component = enemy.getComponent(enemyClass);
                        if (component instanceof Oneal) {
                            ((Oneal) component).setPlayer(playerComponent);
                            System.out.println("Player reference set for " + enemyClass.getSimpleName());
                        }
                    }
                }
            }
        }
        PlayerAIController playerAI = new PlayerAIController(playerComponent, gameGMap, allEnemyEntities, gamePane);


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
                //Muốn dùng AI thì bỏ comment
//                playerAI.update(now);
                playerComponent.onUpdate(1.0 / 60.0);

                // Use player's method to check buff collisions
                playerComponent.checkBuffCollision(buffEntities, gamePane);

                // Update tất cả các loại kẻ địch với một vòng lặp duy nhất
                double deltaTime = 1.0 / 60.0;

                // Duyệt qua tất cả các loại kẻ địch
                for (Map.Entry<Class<? extends Enemy>, List<Entity>> entry : enemyEntities.entrySet()) {
                    Class<? extends Enemy> enemyClass = entry.getKey();
                    List<Entity> entities = entry.getValue();

                    for (Entity enemy : new ArrayList<>(entities)) {
                        if (enemy.getComponentOptional(enemyClass).isPresent()) {
                            Enemy enemyComponent = enemy.getComponent(enemyClass);
                            enemyComponent.onUpdate(deltaTime);
                        }
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