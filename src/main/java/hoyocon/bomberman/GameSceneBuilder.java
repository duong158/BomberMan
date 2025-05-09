package hoyocon.bomberman;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.Camera.CameraFrog;
import hoyocon.bomberman.Camera.CameraStorm;
import hoyocon.bomberman.EntitiesState.State;
import hoyocon.bomberman.Map.GMap;
import hoyocon.bomberman.Map.Map1;
import hoyocon.bomberman.Object.EnemyGroup.*;
import hoyocon.bomberman.Object.Player;
import hoyocon.bomberman.AI.PlayerAIController;

import java.io.IOException;
import java.util.*;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.Group;
import hoyocon.bomberman.Camera.Camera;

import hoyocon.bomberman.Buff.Flame;
import hoyocon.bomberman.Buff.Speed;
import hoyocon.bomberman.Object.BuffEntity;
import hoyocon.bomberman.Buff.BuffGeneric;
import hoyocon.bomberman.Map.GMap;
import hoyocon.bomberman.StatusBar; // Thêm import cho StatusBar
import javafx.util.Duration;

public class GameSceneBuilder {
    public static PlayerAIController playerAI;
    private static boolean autoPlayEnabled = false;
    private static Player lastPlayer;
    public static Boss boss;

    private static final double screenWidth = 1920;
    private static final double screenHeight = 1080;
    private static final double speed = 10;

    // Thêm các biến theo dõi trạng thái phím
    private static boolean isUpPressed = false;
    private static boolean isDownPressed = false;
    private static boolean isLeftPressed = false;
    private static boolean isRightPressed = false;
    // Biến theo dõi trạng thái người chơi để thêm âm thanh.
    private static boolean wasMoving = false;

    private static MediaPlayer backgroundMusicPlayer;
    private static MediaPlayer gameOverMusicPlayer;
    private static boolean musicEnabled = true;

    // Thêm biến static cho pause menu
    private static Pane pauseMenu = null;

    public static Group gameWorld = new Group();

    public static CameraFrog cameraFrog;

    public static CameraStorm cameraStorm;

    public static Camera camera;

    public static AnimationTimer gameLoop; // Lưu tham chiếu đến game loop

    // Danh sách quản lý Transitions và Timers để pause/resume
    private static final List<PauseTransition> pauseTransitions = new ArrayList<>();
    private static final List<AnimationTimer> animationTimers = new ArrayList<>();

    public static void registerPauseTransition(PauseTransition t) {
        pauseTransitions.add(t);
    }

    public static void registerAnimationTimer(AnimationTimer timer) {
        animationTimers.add(timer);
    }

    public static void unregisterTransition(PauseTransition t) {
        pauseTransitions.remove(t);
    }
    public static void unregisterTimer(AnimationTimer t) {
        animationTimers.remove(t);
    }

    /** Pause tất cả Transitions và Timers */
    public static void pauseAll() {
        pauseTransitions.forEach(PauseTransition::pause);
        animationTimers.forEach(AnimationTimer::stop);
    }

    /** Resume tất cả Transitions và Timers */
    public static void resumeAll() {
        pauseTransitions.forEach(PauseTransition::play);
        animationTimers.forEach(AnimationTimer::start);
    }

    // Quản lý buff.
    public static List<BuffEntity> buffEntities = new ArrayList<>();

    /** Danh sách quản lý các Pane explosion để kiểm tra va chạm */
    public static List<Pane> explosionEntities = new ArrayList<>();
    public static List<Pane> bombEntities = new ArrayList<>();

    // Thay thế các danh sách riêng biệt bằng một Map để quản lý tất cả các loại kẻ địch
    public static Map<Class<? extends Enemy>, List<Entity>> enemyEntities = new HashMap<>();
    // Add this new list to store all enemies in one place
    public static List<Enemy> allEnemyEntities = new ArrayList<>();

    public static void addBuffToMap(Pane gamePane, BuffGeneric buff, double x, double y) {
        BuffEntity buffEntity = new BuffEntity(buff, x, y);
        buffEntities.add(buffEntity); // Lưu buff vào danh sách
        gamePane.getChildren().add(buffEntity.getImageView()); // Thêm hình ảnh buff vào gamePane
        gameWorld.getChildren().add(buffEntity.getImageView());
    }
    public static void spawnBalloonAt(int row, int col, GMap gameGMap, Pane gamePane, Group gameWorld) {
        spawnEnemy(gamePane, gameWorld, gameGMap, row, col, Balloon.class, Balloon::new);
    }
    // Method chung để spawn bất kỳ loại kẻ địch nào
    private static <T extends Enemy> void spawnEnemy(
            Pane gamePane, Group gameWorld, GMap gameGMap,
            int row, int col, Class<T> enemyClass,
            EnemyFactory<T> factory) {
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
                gameWorld.getChildren().add(enemyEntity.getViewComponent().getParent());
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
        // 1. Dừng game loop và xóa sạch trạng thái cũ
        if (gameLoop != null) {
            gameLoop.stop();
            SfxManager.stopWalk();
        }
        buffEntities.clear();
        enemyEntities.clear();
        allEnemyEntities.clear();
        bombEntities.clear();
        explosionEntities.clear();
        initializeMusic();
        initializeGameOverMusic();

        // → Reset translate cũ của gameWorld
        gameWorld = new Group();
        gameWorld.setTranslateX(0);
        gameWorld.setTranslateY(0);

        // 2. Reset biến tĩnh về giá trị ban đầu
//        Player.level = 1;      // nếu level là biến public static
        Map1.MOBNUMS = 20;      // về số quái khởi tạo

        // 3. Xây dựng scene va camera mới từ vị trí start
        Scene scene = buildGameScene(GMap.TILE_SIZE, GMap.TILE_SIZE);
        if (cameraFrog != null) {
            cameraFrog.reset();
        }
        if (camera != null) {
            camera.reset();
        }

        // 4. Thiết lập thuộc tính cho player vừa được thêm vào scene
        lastPlayer.setLives(Player.getLives());
        lastPlayer.setSpeed(Player.getSpeed());
        lastPlayer.setMaxBombs(Player.getMaxBombs());
        lastPlayer.setFlameRange(Player.getFlameRange());

        ((Pane)scene.getRoot()).requestFocus();
        return scene;
    }

    private static Scene buildGameScene(double startX, double startY) {
        // Container chính cho toàn bộ scene
        Pane gamePane = new Pane();

        Pane gameWorldContainer = new Pane();

        GameSceneBuilder.explosionEntities.clear();

        gamePane.setStyle("-fx-background-color: black;");

        // Thêm gameWorld và uiPane vào gamePane
        Pane uiPane = new Pane(); // UI layer for StatusBar

        uiPane.setStyle("-fx-background-color: transparent;");

        // Thêm fogPane vào cùng container với gameWorld
        Pane fogPane = new Pane();
        gameWorldContainer.getChildren().addAll(gameWorld, fogPane);
        gamePane.getChildren().addAll(gameWorldContainer, uiPane);

        gamePane.setFocusTraversable(true);

        // Tạo và hiển thị map
        GMap gameGMap;
        if (Player.getLevel() == 4) {
            // Sử dụng boss room map
            gameGMap = new GMap(Map1.getBossRoomMapData());
            System.out.println("Loading Boss Room Map for Level 4");
        } else {
            // Sử dụng map thông thường
            gameGMap = new GMap(Map1.getMapData(60, 40, 0.3f));
            System.out.println("Loading Regular Map for Level " + Player.getLevel());
        }
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
                spawnEnemy(gamePane, gameWorld, gameGMap, position[0], position[1], Balloon.class, Balloon::new);
            }

            // Spawn passes
            List<int[]> passPositions = gameGMap.getESpawnPositions(GMap.PASS);
            System.out.println("Found " + passPositions.size() + " pass positions in map");
            for (int[] position : passPositions) {
                spawnEnemy(
                        gamePane,
                        gameWorld,
                        gameGMap,
                        position[0],
                        position[1],
                        Pass.class,
                        (row, col) -> new Pass(row, col, gameGMap, gamePane, gameWorld)
                );
            }

            // Spawn oneals (nếu có)
            List<int[]> onealPositions = gameGMap.getESpawnPositions(GMap.ONEAL);
            System.out.println("Found " + onealPositions.size() + " oneal positions in map");
            for (int[] position : onealPositions) {
                spawnEnemy(gamePane, gameWorld, gameGMap, position[0], position[1], Oneal.class, Oneal::new);
            }

            List<int[]> dahlPositions = gameGMap.getESpawnPositions(GMap.DAHL);
            System.out.println("Found " + dahlPositions.size() + " dahl positions in map");
            for (int[] position : dahlPositions) {
                spawnEnemy(gamePane, gameWorld, gameGMap, position[0], position[1], Dahl.class, Dahl::new);
            }

            List<int[]> doriaPositions = gameGMap.getESpawnPositions(GMap.DORIA);
            System.out.println("Found " + doriaPositions.size() + " doria positions in map");
            for (int[] position : doriaPositions) {
                spawnEnemy(gamePane, gameWorld, gameGMap, position[0], position[1], Doria.class, Doria::new);
            }

            // Có thể dễ dàng thêm các loại kẻ địch mới ở đây

        } catch (Exception e) {
            System.err.println("Error setting up enemies: " + e.getMessage());
            e.printStackTrace();
        }
        // Tạo entity và thêm Player component
        Entity playerEntity = new Entity();
        final Player playerComponent = new Player();
        playerEntity.addComponent(playerComponent);
        if (Player.getLevel() == 4) {
            try {
                // Vị trí center của boss trong map
                int bossRow = 23/2-6;  // Hàng thứ 7 (index từ 0)
                int bossCol = 41-10;  // Cột thứ 9 (index từ 0)

                // Tạo instance của Boss
                boss = new Boss(bossCol, bossRow);
                boss.setMain(playerComponent);

                boss.setGameReferences(gameWorld, gamePane, gameGMap);

                // Tạo entity và thêm vào gameWorld
                Entity bossEntity = boss.createEntity();
                gameWorld.getChildren().add(bossEntity.getViewComponent().getParent());

                // Lưu trữ boss để xử lý logic sau này nếu cần
                // Ví dụ: có thể thêm vào một danh sách Boss riêng
                System.out.println("Boss spawned at row=" + bossRow + ", col=" + bossCol);
            } catch (Exception e) {
                System.err.println("Error spawning boss: " + e.getMessage());
                e.printStackTrace();
            }
        }



        // Set the game map for collision detection
        playerComponent.setGameMap(gameGMap);

        // Đặt vị trí ban đầu
        startX = GMap.TILE_SIZE;
        startY = GMap.TILE_SIZE;
        playerEntity.setPosition(startX, startY);

        // Thêm playerEntity vào gameWorld thay vì gamePane
        gameWorld.getChildren().add(playerEntity.getViewComponent().getParent());
        lastPlayer = playerComponent;

        //them thanh mau
        StatusBar statusBar = new StatusBar(playerComponent);
        statusBar.setTranslateX(screenWidth - 270);
        statusBar.setTranslateY(10);
        uiPane.getChildren().add(statusBar);
        System.out.println("StatusBar added to uiPane in buildGameScene at " + statusBar.getTranslateX() + ", " + statusBar.getTranslateY());

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

        playerAI = new PlayerAIController(playerComponent, gameGMap, allEnemyEntities, gamePane);

        // Tính kích thước thế giới game
        int worldWidth = gameGMap.width * (int)GMap.TILE_SIZE;
        int worldHeight = gameGMap.height * (int)GMap.TILE_SIZE;

        if (Player.getLevel() % 2 == 0 &&  Player.getLevel() % 4 != 0 ) {
            camera = null;
            cameraFrog = new CameraFrog(fogPane,
                    gameWorld,
                    playerEntity.getViewComponent().getParent(),
                    (int)screenWidth,
                    (int)screenHeight,
                    worldWidth,
                    worldHeight
            );
        } else if (Player.getLevel() % 3 == 0 || Player.getLevel() % 5 == 0 ) {
            camera = null;
            cameraFrog = null;
            cameraStorm = new CameraStorm(fogPane,
                    gameWorld,
                    playerEntity.getViewComponent().getParent(),
                    (int)screenWidth,
                    (int)screenHeight,
                    worldWidth,
                    worldHeight
            );
        } else {
            cameraFrog = null;
            cameraStorm = null;
            camera = new Camera(gameWorld,
                    playerEntity.getViewComponent().getParent(),
                    (int)screenWidth,
                    (int)screenHeight,
                    worldWidth,
                    worldHeight
            );

        }

        Scene scene = new Scene(gamePane, screenWidth, screenHeight);

        // Ẩn con trỏ chuột khi vào game
//        scene.setCursor(Cursor.NONE);

        // Focus
        gamePane.setOnMouseClicked(e -> gamePane.requestFocus());
        gamePane.requestFocus();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean moved = false;

                if (playerComponent.getState() != State.DEAD) {
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

                    if (!moved) {
                        playerComponent.stop();
                    }
                }

                if (moved && !wasMoving) {
                    SfxManager.playWalk();
                    wasMoving = true;
                } else if (!moved && wasMoving) {
                    SfxManager.stopWalk();
                    wasMoving = false;
                }

                //Muốn dùng AI thì bỏ comment
                if (autoPlayEnabled) {
                    playerAI.update(now);
                }
                if(boss != null) {
                    boss.onUpdate(1.0/60.0);
                }
                playerComponent.onUpdate(1.0 / 60.0);

                // Buff collision
                playerComponent.checkBuffCollision(buffEntities, gamePane);

                // Bounds of player
                Bounds playerBounds = playerEntity.getViewComponent().getParent().getBoundsInParent();

                for (Pane p : bombEntities) {

                    if (!(p instanceof Player.BombPane)) continue;
                    Player.BombPane b = (Player.BombPane) p;
                    int playerTileX = GMap.pixelToTile(playerEntity.getX());
                    int playerTileY = GMap.pixelToTile(playerEntity.getY());
                    int playerTileX1 = GMap.pixelToTile(playerEntity.getX() + 45);
                    int playerTileY1 = GMap.pixelToTile(playerEntity.getY() + 45);
                    int bombTileX = GMap.pixelToTile(b.getLayoutX());
                    int bombTileY = GMap.pixelToTile(b.getLayoutY());

                    int dx = 0, dy = 0;
                    if (playerTileX != bombTileX || playerTileY != bombTileY && playerTileX1 == playerTileX && playerTileY1 == playerTileY) {
                        if (isUpPressed && playerTileX == bombTileX && playerTileY == bombTileY + 1) {
                            dy = -1;
                        } else if (isDownPressed && playerTileX == bombTileX && playerTileY == bombTileY - 1) {
                            dy = +1;
                        } else if (isLeftPressed && playerTileY == bombTileY && playerTileX == bombTileX + 1) {
                            dx = -1;
                        } else if (isRightPressed && playerTileY == bombTileY && playerTileX == bombTileX - 1) {
                            dx = +1;
                        }
                    }

                    if ((dx != 0 || dy != 0)) {
                        int tx = bombTileX + dx;
                        int ty = bombTileY + dy;

                        boolean tileEmpty = gameGMap.isWalkable(ty, tx)
                                && GameSceneBuilder.bombEntities.stream().noneMatch(o ->
                                GMap.pixelToTile(o.getLayoutX()) == tx &&
                                        GMap.pixelToTile(o.getLayoutY()) == ty)
                                && GameSceneBuilder.enemyEntities.values().stream().flatMap(List::stream)
                                .noneMatch(e ->
                                        GMap.pixelToTile(e.getX()) == tx &&
                                                GMap.pixelToTile(e.getY()) == ty);

                        if (tileEmpty && !b.isSliding()) {
                            b.startSliding(dx, dy);
                        }
                    }
                }


                // Player vs Flame
//                for (Pane flamePane : explosionEntities) {
//                    Bounds flameBounds = flamePane.getBoundsInParent();
//                    double shrink = 2; // số pixel muốn thu nhỏ mỗi cạnh
//                    Bounds customFlameBounds = new BoundingBox(
//                            flameBounds.getMinX() + shrink,
//                            flameBounds.getMinY() + shrink,
//                            Math.max(0, flameBounds.getWidth() - 2 * shrink),
//                            Math.max(0, flameBounds.getHeight() - 2 * shrink)
//                    );
//                    if (customFlameBounds.intersects(playerBounds)) {
//                        if (!playerComponent.isInvincible()&& !playerComponent.isFlamePassActive() && playerComponent.getState() != State.DEAD) {
//                            playerComponent.setState(State.DEAD);
//                            if (playerComponent.hit()) {
//                                pauseBackgroundMusic();
//                                playGameOverMusic();
//                                PauseTransition deathDelay = new PauseTransition(Duration.seconds(1)); // Adjust time as needed
//                                deathDelay.setOnFinished(event -> {
//                                    stop(); // Stop game loop after animation completes
//
//                                    try {
//                                        Parent root = FXMLLoader.load(GameSceneBuilder.class.getResource("/FXML/GameOver.fxml"));
//                                        Scene gameOverScene = new Scene(root, screenWidth, screenHeight);
//
//                                        // Add null check before accessing window/stage
//                                        if (gamePane.getScene() != null && gamePane.getScene().getWindow() != null) {
//                                            Stage stage = (Stage) gamePane.getScene().getWindow();
//                                            stage.setScene(gameOverScene);
//                                        } else {
//                                            System.err.println("Cannot show game over screen: Scene or Window is null");
//                                        }
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                });
//                                deathDelay.play();
//                            } else {
//                                PauseTransition deathDelay = new PauseTransition(Duration.seconds(1.0));
//                                deathDelay.setOnFinished(event -> {
//                                    // Khôi phục vị trí ban đầu
//                                    playerEntity.setPosition(48, 48);
//
//                                    // Trigger invincibility sau khi hồi sinh
//                                    playerComponent.triggerInvincibility();
//
//                                    // Đặt lại trạng thái
//                                    playerComponent.setState(State.IDLE);
//                                    playerAI.resetAIState();
//
//                                });
//                                deathDelay.play();
//                            }
//                        }
//                    }
//                }

                // Flame vs Enemy (với animation chết)
                for (Pane flamePane : explosionEntities) {
                    Bounds flameBounds = flamePane.getBoundsInParent();
                    for (Map.Entry<Class<? extends Enemy>, List<Entity>> entry : enemyEntities.entrySet()) {
                        List<Entity> list = entry.getValue();
                        Iterator<Entity> it = list.iterator();
                        while (it.hasNext()) {
                            Entity enemyEntity = it.next();
                            Bounds enemyBounds = enemyEntity.getViewComponent().getParent().getBoundsInParent();
                            if (flameBounds.intersects(enemyBounds)) {
                                Enemy enemyComp = enemyEntity.getComponent(entry.getKey());

                                if (!enemyComp.isDead()) {
                                    enemyComp.die(); // play death animation
                                    Entity e = enemyEntity;
                                    List<Entity> refList = list;
                                    // Hẹn xóa khi animation 'dead' đã hoàn thành (1.5s)
                                    PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                                    delay.setOnFinished(event -> {
                                        Platform.runLater(() -> {
                                            refList.remove(e);
                                            gameWorld.getChildren().remove(e.getViewComponent().getParent());
                                            e.removeFromWorld();
                                        });
                                    });
                                    delay.play();
                                }
                            }
                        }
                    }
                }

                // Enemy vs Player
                for (Map.Entry<Class<? extends Enemy>, List<Entity>> entry : enemyEntities.entrySet()) {
                    for (Entity enemyEntity : entry.getValue()) {
                        Enemy enemyComp = enemyEntity.getComponent(entry.getKey());
                        if (enemyComp.isDead())
                            continue;   // bỏ qua enemy đã chết
                        Bounds enemyBounds = enemyEntity.getViewComponent().getParent().getBoundsInParent();
                        double shrink = 2; // số pixel muốn thu nhỏ mỗi cạnh (ví dụ 8)
                        Bounds customEnemyBounds = new BoundingBox(
                                enemyBounds.getMinX() + shrink,
                                enemyBounds.getMinY() + shrink,
                                Math.max(0, enemyBounds.getWidth() - 2 * shrink),
                                Math.max(0, enemyBounds.getHeight() - 2 * shrink)
                        );

                        if (customEnemyBounds.intersects(playerBounds)) {
                            if (!playerComponent.isInvincible() && playerComponent.getState() != State.DEAD) {
                                playerComponent.setState(State.DEAD);
                                if (playerComponent.hit()) {
                                    pauseBackgroundMusic();
                                    playGameOverMusic();
                                    PauseTransition deathDelay = new PauseTransition(Duration.seconds(1)); // Adjust time as needed
                                    deathDelay.setOnFinished(event -> {
                                        stop(); // Stop game loop after animation completes

                                        try {
                                            Parent root = FXMLLoader.load(GameSceneBuilder.class.getResource("/FXML/GameOver.fxml"));
                                            Scene gameOverScene = new Scene(root, screenWidth, screenHeight);

                                            // Add null check before accessing window/stage
                                            if (gamePane.getScene() != null && gamePane.getScene().getWindow() != null) {
                                                Stage stage = (Stage) gamePane.getScene().getWindow();
                                                stage.setScene(gameOverScene);
                                            } else {
                                                System.err.println("Cannot show game over screen: Scene or Window is null");
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    });
                                    deathDelay.play();
                                } else {
                                    // Chạy animation chết và hồi sinh
                                    PauseTransition deathDelay = new PauseTransition(Duration.seconds(1.0));
                                    deathDelay.setOnFinished(event -> {
                                        // Khôi phục vị trí ban đầu
                                        playerEntity.setPosition(48, 48);

                                        // Trigger invincibility sau khi hồi sinh
                                        playerComponent.triggerInvincibility();

                                        // Đặt lại trạng thái
                                        playerComponent.setState(State.IDLE);
                                        playerAI.resetAIState();

                                    });
                                    deathDelay.play();
                                }
                            }
                        }
                    }
                }

                // Update tất cả enemy
                double deltaTime = 1.0 / 60.0;
                for (Map.Entry<Class<? extends Enemy>, List<Entity>> entry : enemyEntities.entrySet()) {
                    Class<? extends Enemy> enemyClass = entry.getKey();
                    for (Entity enemy : new ArrayList<>(entry.getValue())) {
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
            if (playerComponent.getState() != State.DEAD) {
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
                } if (event.getCode() == KeyCode.F1) {
                    playerAI.resetAIState();
                    toggleAutoPlay();
                    return;  // ngăn xử lý tiếp
                } if (event.getCode() == KeyCode.F2) {
                    playerEntity.setPosition(58*GMap.TILE_SIZE, 38*GMap.TILE_SIZE);
                }
            }

            // Always allow ESC key regardless of player state
            if (event.getCode() == KeyCode.ESCAPE) {
                if (gameLoop != null) gameLoop.stop();
                SfxManager.stopWalk();
                showPauseMenu(uiPane);
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

    // Phương thức tạo kẻ địch dựa trên loại
    private static Enemy createEnemy(String type, double x, double y) {
        switch (type.toLowerCase()) {
            case "balloon":
                return new Balloon((int) x, (int) y);
            case "oneal":
                return new Oneal((int) x, (int) y);
            case "dahl":
                return new Dahl((int) x, (int) y);
            case "doria":
                return new Doria((int) x, (int) y);
            case "pass":
                return new Pass((int) x, (int) y, null, null, null); // Thay null bằng các tham số phù hợp
            default:
                System.err.println("Unknown enemy type: " + type);
                return null;
        }
    }

    /**
     * Bật hoặc tắt AI auto‑play.
     */
    public static void setAutoPlay(boolean enabled) {
        autoPlayEnabled = enabled;
        System.out.println("AutoPlay " + (enabled ? "ENABLED" : "DISABLED"));
    }

    /**
     * Đảo trạng thái auto‑play.
     */
    public static void toggleAutoPlay() {
        setAutoPlay(!autoPlayEnabled);
    }

    private static void showPauseMenu(Pane uiPane) {
        if (pauseMenu != null) return;
        Font.loadFont(
                GameSceneBuilder.class
                        .getResource("/fonts/PressStart2P-Regular.ttf")
                        .toExternalForm(),
                10
        );
//        pauseBackgroundMusic();

        // 1. Dừng gameLoop và pause tất cả Transitions/Timers
        if (gameLoop != null) {
            SfxManager.stopWalk();
            gameLoop.stop();
        }
        pauseAll();

        // 2. Tải FXML pause menu
        try {
            FXMLLoader loader = new FXMLLoader(
                    GameSceneBuilder.class.getResource("/FXML/pause_menu.fxml"));
            AnchorPane menuPane = loader.load();
            PauseMenuController ctrl = loader.getController();
            ctrl.setUiPane(uiPane);

            // 3. Tạo overlay mờ full-screen
            Pane overlay = new Pane();
            overlay.setPrefSize(screenWidth, screenHeight);
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");

            // 4. Wrap overlay + menuPane thành pauseMenu
            pauseMenu = new Pane();
            pauseMenu.setPrefSize(screenWidth, screenHeight);
            pauseMenu.getChildren().setAll(overlay, menuPane);

            // 5. Center menuPane trong màn hình
            double mx = (screenWidth - menuPane.getPrefWidth()) / 2;
            double my = (screenHeight - menuPane.getPrefHeight()) / 2;
            menuPane.setLayoutX(mx);
            menuPane.setLayoutY(my);


            // 6. Thêm vào UI layer
            uiPane.getChildren().add(pauseMenu);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void hidePauseMenu(Pane uiPane) {
        if (pauseMenu != null) {
            // 1. Remove pause menu khỏi UI layer
            uiPane.getChildren().remove(pauseMenu);
            pauseMenu = null;
            resumeAll();

            if (gameLoop != null) {
                if (playerAI != null) {
                    playerAI.resetTimer();
                }
                gameLoop.start();
            }
        }
//        if (musicEnabled) {
//            playBackgroundMusic();
//        }

    }
    public static void initializeMusic() {
        try {
            String musicFile;
            if(Player.getLevel() % 4 == 0)  musicFile = "/assets/music/boss_theme.mp3";
            else  musicFile = "/assets/music/battle-theme.mp3"; // Path to music file in resources folder
            Media backgroundMusic = new Media(GameSceneBuilder.class.getResource(musicFile).toExternalForm());
            backgroundMusicPlayer = new MediaPlayer(backgroundMusic);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop indefinitely
            backgroundMusicPlayer.setVolume(0.5); // Set to 50% volume

            if (musicEnabled) {
                backgroundMusicPlayer.play();
            }
            SfxManager.initWalk();
        } catch (Exception e) {
            System.err.println("Error loading background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void initializeGameOverMusic() {
        try {
            String musicFile = "/assets/music/deadgod.mp3"; // Path to game over music
            Media gameOverMusic = new Media(GameSceneBuilder.class.getResource(musicFile).toExternalForm());
            gameOverMusicPlayer = new MediaPlayer(gameOverMusic);
            gameOverMusicPlayer.setVolume(0.5);
        } catch (Exception e) {
            System.err.println("Error loading game over music: " + e.getMessage());
        }
    }

    /**
     * Play the background music if enabled.
     */
    public static void playBackgroundMusic() {
        if (musicEnabled && backgroundMusicPlayer != null) {
            backgroundMusicPlayer.play();
        }
    }

    /**
     * Pause the background music.
     */
    public static void pauseBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.pause();
        }
    }

    /**
     * Stop the background music.
     */
    public static void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
        }
    }

    /**
     * Play game over music.
     */
    public static void playGameOverMusic() {
        stopBackgroundMusic();
        if (musicEnabled && gameOverMusicPlayer != null) {
            gameOverMusicPlayer.play();
        }
    }

    /**
     * Toggle music on/off.
     */
    public static void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (musicEnabled) {
            playBackgroundMusic();
        } else {
            pauseBackgroundMusic();
        }
    }

    /**
     * Set the volume of the background music.
     * @param volume Volume level (0.0 to 1.0)
     */
    public static void setMusicVolume(double volume) {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(Math.min(1.0, Math.max(0.0, volume)));
        }
        if (gameOverMusicPlayer != null) {
            gameOverMusicPlayer.setVolume(Math.min(1.0, Math.max(0.0, volume)));
        }
    }

    /**
     * Check if music is enabled.
     * @return true if music is enabled
     */
    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static void resetMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.dispose(); // Release all resources
            backgroundMusicPlayer = null;
        }

        if (gameOverMusicPlayer != null) {
            gameOverMusicPlayer.stop();
            gameOverMusicPlayer.dispose(); // Release all resources
            gameOverMusicPlayer = null;
        }
    }
}
