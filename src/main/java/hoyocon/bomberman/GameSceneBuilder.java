package hoyocon.bomberman;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.EntitiesState.State;
import hoyocon.bomberman.Map.GMap;
import hoyocon.bomberman.Map.Map1;
import hoyocon.bomberman.Object.EnemyGroup.*;
import hoyocon.bomberman.Object.Player;
import hoyocon.bomberman.AI.PlayerAIController;
import hoyocon.bomberman.Object.Bomb;

import java.io.IOException;
import java.util.*;

import hoyocon.bomberman.Object.Player;
import hoyocon.bomberman.Save.*;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.Group;
import hoyocon.bomberman.Camera.Camera;

import hoyocon.bomberman.Buff.Flame;
import hoyocon.bomberman.Buff.Speed;
import hoyocon.bomberman.Object.BuffEntity;
import hoyocon.bomberman.Buff.BuffGeneric;
import hoyocon.bomberman.Map.GMap;
import javafx.util.Duration;

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


    public static AnimationTimer gameLoop; // Lưu tham chiếu đến game loop




    // Quản lí buff.
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
        // Reset về vị trí ban đầu
        savedX = 195;
        savedY = 70;
        return buildGameScene(savedX, savedY);
    }

    public static Scene buildContinueScene() {
        GameState savedState = SaveManager.load();
        if (savedState == null) {
            return buildNewGameScene();
        }
        return buildGameSceneWithState(savedState);
    }

    private static Scene buildGameScene(double startX, double startY) {
        // Container chính cho toàn bộ scene
        Pane gamePane = new Pane();
        GameSceneBuilder.explosionEntities.clear();

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
            System.out.println("Found " + onealPositions.size() + " oneal positions in map");
            for (int[] position : dahlPositions) {
                spawnEnemy(gamePane, gameWorld, gameGMap, position[0], position[1], Dahl.class, Dahl::new);
            }

            List<int[]> doriaPositions = gameGMap.getESpawnPositions(GMap.DORIA);
            System.out.println("Found " + doriaPositions.size() + " oneal positions in map");
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
                //Muốn dùng AI thì bỏ comment
                playerAI.update(now);
                playerComponent.onUpdate(1.0 / 60.0);

                // Buff collision
                playerComponent.checkBuffCollision(buffEntities, gamePane);

                // Bounds of player
                Bounds playerBounds = playerEntity.getViewComponent().getParent().getBoundsInParent();

                // Player vs Flame
//                for (Pane flamePane : explosionEntities) {
//                    if (flamePane.getBoundsInParent().intersects(playerBounds)) {
//                        if (!playerComponent.isInvincible()&& playerComponent.getState() != State.DEAD) {
//                            playerComponent.setState(State.DEAD);
//                            if (playerComponent.hit()) {
//                                PauseTransition deathDelay = new PauseTransition(Duration.seconds(1)); // Adjust time as needed
//                                deathDelay.setOnFinished(event -> {
//                                    stop(); // Stop game loop after animation completes
//
//                                    try {
//                                        Parent root = FXMLLoader.load(GameSceneBuilder.class.getResource("/hoyocon/bomberman/GameOver.fxml"));
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
                        if (enemyBounds.intersects(playerBounds)) {
                            if (!playerComponent.isInvincible() && playerComponent.getState() != State.DEAD) {
                                playerComponent.setState(State.DEAD);
                                if (playerComponent.hit()) {
                                    PauseTransition deathDelay = new PauseTransition(Duration.seconds(1)); // Adjust time as needed
                                    deathDelay.setOnFinished(event -> {
                                        stop(); // Stop game loop after animation completes

                                        try {
                                            Parent root = FXMLLoader.load(GameSceneBuilder.class.getResource("/hoyocon/bomberman/GameOver.fxml"));
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
                }
            }

            // Always allow ESC key regardless of player state
            if (event.getCode() == KeyCode.ESCAPE) {
                // 1. Thu thập GameState
                GameState state = new GameState();
                // Player
                state.playerX = playerEntity.getX();
                state.playerY = playerEntity.getY();
                state.lives    = playerComponent.getLives();
                state.speed    = playerComponent.getSpeed();
                state.bombCount= playerComponent.getBombCount();
                state.maxBombs = playerComponent.getMaxBombs();
                state.flameRange = playerComponent.getFlameRange();
                state.activeBuffs = playerComponent.getActiveBuffs();

                // Enemies
                state.enemies = new ArrayList<>();
                for (Enemy e : allEnemyEntities) {
                    EnemyState es = new EnemyState();
                    es.type = e.getClass().getSimpleName();
                    es.x    = e.getEntity().getX();
                    es.y    = e.getEntity().getY();
                    state.enemies.add(es);
                }

                // Map
                state.mapData = gameGMap.getMapDataArray();

                // Buffs trên bản đồ
                state.buffs = new ArrayList<>();
                for (BuffEntity be : buffEntities) {
                    BuffState bs = new BuffState();
                    bs.buffType = be.getBuff().getType();
                    bs.x = be.getImageView().getX();
                    bs.y = be.getImageView().getY();
                    state.buffs.add(bs);
                }

                // Bombs đang đặt (theo player)
                state.bombs = new ArrayList<>();
                for (Pane bp : playerComponent.getBombPanes()) {
                    BombState bms = new BombState();
                    bms.x = bp.getLayoutX(); bms.y = bp.getLayoutY();
                    state.bombs.add(bms);
                }

                // 2. Lưu state ra file
                SaveManager.save(state);

                // 3. Dừng gameLoop + về menu
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

    public static Scene buildGameSceneWithState(GameState state) {
        // 1) Tạo pane và world
        Pane gamePane = new Pane();
        explosionEntities.clear();
        gamePane.setStyle("-fx-background-color: black;");
        Group gameWorld = new Group();
        gamePane.getChildren().add(gameWorld);
        gamePane.setFocusTraversable(true);

        // 2) Map từ state.mapData
        GMap gameGMap = new GMap(state.mapData);
        gameGMap.render();
        gameWorld.getChildren().add(gameGMap.getCanvas());

        // 3) Chuẩn bị ánh xạ name→Class và Class→factory
        Map<String, Class<? extends Enemy>> nameToClass = Map.of(
                "Balloon", Balloon.class,
                "Pass",    Pass.class,
                "Oneal",   Oneal.class,
                "Dahl",    Dahl.class,
                "Doria",   Doria.class
        );
        Map<Class<? extends Enemy>, EnemyFactory<? extends Enemy>> factories = new HashMap<>();
        factories.put(Balloon.class, Balloon::new);
        factories.put(Pass.class, (col, row) -> new Pass(row, col, gameGMap, gamePane, gameWorld));
        factories.put(Oneal.class,   (c, r) -> new Oneal(r, c));
        factories.put(Dahl.class,    Dahl::new);
        factories.put(Doria.class,   Doria::new);

        // 4) Spawn lại enemies
        for (EnemyState es : state.enemies) {
            int col = (int)(es.x / GMap.TILE_SIZE);
            int row = (int)(es.y / GMap.TILE_SIZE);
            Class<? extends Enemy> rawCls = nameToClass.get(es.type);
            EnemyFactory<? extends Enemy> rawFac = factories.get(rawCls);
            if (rawCls != null && rawFac != null) {
                @SuppressWarnings("unchecked")
                Class<Enemy> clsT = (Class<Enemy>) rawCls;
                @SuppressWarnings("unchecked")
                EnemyFactory<Enemy> facT = (EnemyFactory<Enemy>) rawFac;

                spawnEnemy(gamePane, gameWorld, gameGMap, row, col, clsT, facT);
            }
        }

        // 5) Tạo Player và set trạng thái
        Entity playerEntity = new Entity();
        Player playerComp = new Player();
        playerComp.setGameMap(gameGMap);
        playerComp.setLives(state.lives);
        playerComp.setSpeed(state.speed);
        playerComp.setMaxBombs(state.maxBombs);
        playerComp.setFlameRange(state.flameRange);
        playerComp.setActiveBuffs(state.activeBuffs);
        playerEntity.addComponent(playerComp);
        playerEntity.setPosition(state.playerX, state.playerY);
        gameWorld.getChildren().add(playerEntity.getViewComponent().getParent());

        // 6) Thêm buff tồn
        for (BuffState bs : state.buffs) {
            BuffGeneric buff;
            switch (bs.buffType) {
                case "speed":      buff = new Speed();      break;
                case "flameRange": buff = new Flame();      break;
                default:           continue;
            }
            BuffEntity be = new BuffEntity(buff, bs.x, bs.y);
            buffEntities.add(be);
            gamePane.getChildren().add(be.getImageView());
        }

        // 7) Thêm bombs chưa nổ
        long now = System.currentTimeMillis();
        for (BombState bs : state.bombs) {
            long elapsed = now - bs.timePlaced;
            long remaining = bs.timeRemaining - elapsed;
            if (remaining <= 0) continue;

            // Tạo bom và texture
            Bomb bombComp = new Bomb(playerComp, gamePane);
            AnimatedTexture tex = bombComp.getTexture();
            tex.loop();

            Pane bombPane = new Pane(tex);
            bombPane.setPrefSize(GMap.TILE_SIZE, GMap.TILE_SIZE);
            bombPane.setLayoutX(bs.x);
            bombPane.setLayoutY(bs.y);
            gamePane.getChildren().add(bombPane);

            // Lên lịch nổ
            PauseTransition delay = new PauseTransition(Duration.millis(remaining));
            delay.setOnFinished(e -> {
                bombComp.explode();
                gamePane.getChildren().remove(bombPane);
            });
            delay.play();
        }

        // 8) Tạo Scene và return
        Scene scene = new Scene(gamePane, screenWidth, screenHeight);
        scene.setCursor(Cursor.NONE);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W -> isUpPressed = true;
                case S -> isDownPressed = true;
                case A -> isLeftPressed = true;
                case D -> isRightPressed = true;
                case SPACE -> playerComp.placeBomb(gamePane);
                case ESCAPE -> {
                    // Thu thập bom
                    state.bombs = playerComp.getBombPanes().stream().map(bp -> {
                        BombState b = new BombState();
                        b.x = bp.getLayoutX();
                        b.y = bp.getLayoutY();
                        b.timePlaced = bp.getTimePlaced();
                        b.timeRemaining = 2000;
                        return b;
                    }).toList();
                    // Cập nhật player pos & trạng thái nếu cần
                    state.playerX = playerEntity.getX();
                    state.playerY = playerEntity.getY();
                    state.lives   = playerComp.getLives();

                    SaveManager.save(state);
                    gameLoop.stop();
                    try {
                        Parent menuRoot = FXMLLoader.load(
                                GameSceneBuilder.class.getResource("/hoyocon/bomberman/Menu-view.fxml")
                        );
                        ((Stage) scene.getWindow()).setScene(new Scene(menuRoot, screenWidth, screenHeight));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // --- 1) Tạo và start gameLoop lại ---
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                boolean moved = false;

                if (playerComp.getState() != State.DEAD) {
                    if (isUpPressed) {
                        moved = playerComp.moveUp(0.016);
                    }
                    if (isDownPressed) {
                        moved = playerComp.moveDown(0.016);
                    }
                    if (isLeftPressed) {
                        moved = playerComp.moveLeft(0.016);
                    }
                    if (isRightPressed) {
                        moved = playerComp.moveRight(0.016);
                    }

                    if (!moved) {
                        playerComp.stop();
                    }
                }
                //Muốn dùng AI thì bỏ comment
//                playerAI.update(now);
                playerComp.onUpdate(1.0 / 60.0);

                // Buff collision
                playerComp.checkBuffCollision(buffEntities, gamePane);

                // Bounds of player
                Bounds playerBounds = playerEntity.getViewComponent().getParent().getBoundsInParent();

                // Player vs Flame
                for (Pane flamePane : explosionEntities) {
                    if (flamePane.getBoundsInParent().intersects(playerBounds)) {
                        if (!playerComp.isInvincible()&& playerComp.getState() != State.DEAD) {
                            if (playerComp.hit()) {
                                System.out.println("Player died by explosion!");
                                stop(); // Dừng game loop
                                try {
                                    Parent root = FXMLLoader.load(GameSceneBuilder.class.getResource("/hoyocon/bomberman/GameOver.fxml"));
                                    Scene gameOverScene = new Scene(root, screenWidth, screenHeight);

                                    // Add null check before accessing window/stage
                                    if (gamePane.getScene() != null && gamePane.getScene().getWindow() != null) {
                                        Stage stage = (Stage) gamePane.getScene().getWindow();
                                        stage.setScene(gameOverScene);
                                    } else {
                                        System.err.println("Cannot show game over screen: Scene or Window is null");
                                        // Consider an alternative method to end the game
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                // Chạy animation chết và hồi sinh
                                playerComp.setState(State.DEAD);

                                PauseTransition deathDelay = new PauseTransition(Duration.seconds(1.0));
                                deathDelay.setOnFinished(event -> {
                                    // Khôi phục vị trí ban đầu
                                    playerEntity.setPosition(48, 48);

                                    // Trigger invincibility sau khi hồi sinh
                                    playerComp.triggerInvincibility();

                                    // Đặt lại trạng thái
                                    playerComp.setState(State.IDLE);

                                });
                                deathDelay.play();
                            }
                        }
                    }
                }

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
                        if (enemyBounds.intersects(playerBounds)) {
                            if (!playerComp.isInvincible() && playerComp.getState() != State.DEAD) {
                                if (playerComp.hit()) {
                                    System.out.println("Player died by enemy collision!");
                                    stop(); // Dừng game loop
                                    try {
                                        Parent root = FXMLLoader.load(GameSceneBuilder.class.getResource("/hoyocon/bomberman/GameOver.fxml"));
                                        Scene gameOverScene = new Scene(root, screenWidth, screenHeight);

                                        // Add null check before accessing window/stage
                                        if (gamePane.getScene() != null && gamePane.getScene().getWindow() != null) {
                                            Stage stage = (Stage) gamePane.getScene().getWindow();
                                            stage.setScene(gameOverScene);
                                        } else {
                                            System.err.println("Cannot show game over screen: Scene or Window is null");
                                            // Consider an alternative method to end the game
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    // Chạy animation chết và hồi sinh
                                    playerComp.setState(State.DEAD);

                                    PauseTransition deathDelay = new PauseTransition(Duration.seconds(1.0));
                                    deathDelay.setOnFinished(event -> {
                                        // Khôi phục vị trí ban đầu
                                        playerEntity.setPosition(48, 48);

                                        // Trigger invincibility sau khi hồi sinh
                                        playerComp.triggerInvincibility();

                                        // Đặt lại trạng thái
                                        playerComp.setState(State.IDLE);

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

        // --- 2) Đăng ký key handlers lại ---
        gamePane.setOnKeyPressed(event -> {
            if (playerComp.getState() != State.DEAD) {
                if (event.getCode() == KeyCode.W)       isUpPressed = true;
                else if (event.getCode() == KeyCode.S)  isDownPressed = true;
                else if (event.getCode() == KeyCode.A)  isLeftPressed = true;
                else if (event.getCode() == KeyCode.D)  isRightPressed = true;
                else if (event.getCode() == KeyCode.SPACE) {
                    playerComp.placeBomb(gamePane);
                }
            }
            if (event.getCode() == KeyCode.ESCAPE) {
                // (copy nguyên logic ESC: thu thập state, SaveManager.save, stop loop, về menu)
            }
        });

        gamePane.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.W)      isUpPressed = false;
            else if (event.getCode() == KeyCode.S) isDownPressed = false;
            else if (event.getCode() == KeyCode.A) isLeftPressed = false;
            else if (event.getCode() == KeyCode.D) isRightPressed = false;
            if (!isUpPressed && !isDownPressed && !isLeftPressed && !isRightPressed) {
                playerComp.stop();
            }
        });

        // --- 3) Đảm bảo focus để nhận phím ---
        gamePane.requestFocus();
        // ... copy nguyên phần thiết lập camera, key handlers, gameLoop từ buildGameScene()
        return scene;
    }


}