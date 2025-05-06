package hoyocon.bomberman.Object;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import hoyocon.bomberman.EntitiesState.EntityType;
import hoyocon.bomberman.EntitiesState.State;
import hoyocon.bomberman.Main;
import hoyocon.bomberman.Map.GMap;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import hoyocon.bomberman.GameSceneBuilder;
import hoyocon.bomberman.Buff.BuffGeneric;
import javafx.scene.layout.Pane;
import hoyocon.bomberman.Object.Bomb;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static hoyocon.bomberman.GameSceneBuilder.camera;
import static hoyocon.bomberman.GameSceneBuilder.gameWorld;

public class Player extends Component {
    // Vị trí người chơi
    private int x, y;

    public static int level = 1;
    private boolean hasExited = false;

    // Thuộc tính người chơi
    private static int lives;
    private static double speed;
    private int bombCount;
    private static int maxBombs;
    private boolean canPlaceBomb;
    private final int FRAMESIZE = 45;
    private final int NUMFRAME = 3;

    // Trạng thái người chơi
    private State state;
    private State lastAni;
    private boolean invincible = false;
    private boolean flamePassActive = false;


    // Hình ảnh và animation
    private AnimatedTexture texture;

    private AnimationChannel walkup;
    private AnimationChannel walkdown;
    private AnimationChannel walkleft;
    private AnimationChannel walkright;

    private AnimationChannel deadAni;

    private AnimationChannel idledown;

    // Buff logic
    private static int flameRange = 1;

    public static int getFlameRange() {
        return flameRange;
    }

    private double baseSpeed = 150;

    // Map collision reference
    private GMap gameGMap;
    private static final double DEFAULT_COLLISION_SPEED = 10;

    public Bounds getBounds() {
        return entity.getViewComponent().getParent().getBoundsInParent();
    }

    // Buff timers
    private Map<String, Long> activeBuffs = new HashMap<>();
    private static final long BUFF_DURATION = 10 * 1000; // 10 seconds in milliseconds

    // Hitbox constants
    private static final int PLAYER_WIDTH = 45;
    private static final int PLAYER_HEIGHT = 45;
    private static final int HITBOX_MARGIN = 0; // Margin to make hitbox slightly smaller than sprite

    private List<BombPane> bombs = new ArrayList<>();

    public static class BombPane extends Pane {
        private long timePlaced;
        public BombPane(AnimatedTexture tex, double x, double y) {
            super(tex);
            setPrefSize(GMap.TILE_SIZE, GMap.TILE_SIZE);
            setLayoutX(x);
            setLayoutY(y);
            this.timePlaced = System.currentTimeMillis();
        }
        public long getTimePlaced() {
            return timePlaced;
        }
    }

    // Get hitbox with margins for better collision detection
    private double[][] getHitboxPoints(double x, double y) {
        return new double[][] {
                {x + HITBOX_MARGIN, y + HITBOX_MARGIN},
                {x + PLAYER_WIDTH - HITBOX_MARGIN, y + HITBOX_MARGIN},
                {x + HITBOX_MARGIN, y + PLAYER_HEIGHT - HITBOX_MARGIN},
                {x + PLAYER_WIDTH - HITBOX_MARGIN, y + PLAYER_HEIGHT - HITBOX_MARGIN},
                {x + PLAYER_WIDTH / 2, y + HITBOX_MARGIN},
                {x + PLAYER_WIDTH / 2, y + PLAYER_HEIGHT - HITBOX_MARGIN},
                {x + HITBOX_MARGIN, y + PLAYER_HEIGHT / 2},
                {x + PLAYER_WIDTH - HITBOX_MARGIN, y + PLAYER_HEIGHT / 2}
        };
    }

    // Enhanced collision detection
    private boolean canMoveTo(double newX, double newY) {
        if (gameGMap == null) return true;

        double[][] hitboxPoints = getHitboxPoints(newX, newY);

        for (double[] point : hitboxPoints) {
            int col = GMap.pixelToTile(point[0]);
            int row = GMap.pixelToTile(point[1]);

            if (row < 0 || row >= gameGMap.height || col < 0 || col >= gameGMap.width || !gameGMap.isWalkable(row, col)) {
                return false;
            }
        }
        return true;
    }

    public Player() {
        this.lives = 1000;
        this.speed = baseSpeed;
        this.bombCount = 0;
        this.maxBombs = 1;
        this.canPlaceBomb = true;
        this.state = State.IDLE;
        this.lastAni = State.IDLE;

        Image upImage = new Image(getClass().getResourceAsStream("/assets/textures/player_up.png"));
        Image downImage = new Image(getClass().getResourceAsStream("/assets/textures/player_down.png"));
        Image leftImage = new Image(getClass().getResourceAsStream("/assets/textures/player_left.png"));
        Image rightImage = new Image(getClass().getResourceAsStream("/assets/textures/player_right.png"));
        Image idleImage = new Image(getClass().getResourceAsStream("/assets/textures/player_down.png"));
        Image deadIma = new Image(getClass().getResourceAsStream("/assets/textures/player_die.png"));

        walkup = new AnimationChannel(upImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(0.5), 0, 2);
        walkdown = new AnimationChannel(downImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(0.5), 0, 2);
        walkleft = new AnimationChannel(leftImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(0.5), 0, 2);
        walkright = new AnimationChannel(rightImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(0.5), 0, 2);
        idledown = new AnimationChannel(idleImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(1), 0, 0);

        deadAni = new AnimationChannel(deadIma, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(1), 0, 2);

        texture = new AnimatedTexture(idledown);
    }

    public void setGameMap(GMap GMap) {
        this.gameGMap = GMap;
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        updateAnimation();
        texture.onUpdate(tpf);
        updateBuffs();
        int pRow = GMap.pixelToTile(entity.getY());
        int pCol = GMap.pixelToTile(entity.getX());
        if(!hasExited && gameGMap.getTileType(pRow,pCol) == GMap.EXIT){
            onExit();
            hasExited = true;
        }
    }

    private void updateAnimation() {
        switch (state) {
            case UP:
                texture.loopNoOverride(walkup);
                break;
            case RIGHT:
                texture.loopNoOverride(walkright);
                break;
            case DOWN:
                texture.loopNoOverride(walkdown);
                break;
            case LEFT:
                texture.loopNoOverride(walkleft);
                break;
            case DEAD:
                texture.loopNoOverride(deadAni);
                break;
            case IDLE:
                texture.loopNoOverride(idledown);
                break;
        }
    }

    private void updateBuffs() {
        long currentTime = System.currentTimeMillis();
        activeBuffs.entrySet().removeIf(entry -> {
            boolean expired = currentTime - entry.getValue() > BUFF_DURATION;
            if (expired) {
                switch (entry.getKey()) {
                    case "speed" -> speed = baseSpeed;
                    case "flameRange" -> flameRange = 1;      // reset về 1 khi flame buff hết
                    case "bomb" -> maxBombs = Math.max(1, maxBombs - 1); // nếu bạn dùng bomb buff có thời hạn
                    case "flamePass"->
                            removeFlamePass();
                }
            }
            return expired;
        });
    }

    public boolean moveUp(double tpf) {
        if(state != State.DEAD) {
            double newX = entity.getX();
            double newY = entity.getY() - speed * tpf;

            if (canMoveTo(newX, newY)) {
                setState(State.UP);
                // Làm tròn nếu gần sát vị trí nguyên
                double snappedY = Math.round(newY);
                if (Math.abs(snappedY - newY) < 0.001) {
                    entity.setY(snappedY);
                } else {
                    entity.translateY(-speed * tpf);
                }
                return true;
            } else {
                setState(State.UP);
                double safeDistance = findSafeDistance(entity.getX(), entity.getY(), 0, -1, speed * tpf);
                if (safeDistance > 0) {
                    entity.translateY(-safeDistance);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean moveDown(double tpf) {
        if(state != State.DEAD) {
            double newX = entity.getX();
            double newY = entity.getY() + speed * tpf;

            if (canMoveTo(newX, newY)) {
                setState(State.DOWN);
                // Làm tròn nếu gần sát vị trí nguyên
                double snappedY = Math.round(newY);
                if (Math.abs(snappedY - newY) < 0.001) {
                    entity.setY(snappedY);
                } else {
                    entity.translateY(speed * tpf);
                }
                return true;
            } else {
                setState(State.DOWN);
                double safeDistance = findSafeDistance(entity.getX(), entity.getY(), 0, 1, speed * tpf);
                if (safeDistance > 0) {
                    entity.translateY(safeDistance);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean moveLeft(double tpf) {
        if(state != State.DEAD) {
            double newX = entity.getX() - speed * tpf;
            double newY = entity.getY();

            if (canMoveTo(newX, newY)) {
                setState(State.LEFT);
                // Làm tròn nếu gần sát vị trí nguyên (hoặc tile)
                double snappedX = Math.round(newX);
                if (Math.abs(snappedX - newX) < 0.001) {
                    entity.setX(snappedX);
                } else {
                    entity.translateX(-speed * tpf);
                }
                return true;
            } else {
                setState(State.LEFT);
                double safeDistance = findSafeDistance(entity.getX(), entity.getY(), -1, 0, speed * tpf);
                if (safeDistance > 0) {
                    entity.translateX(-safeDistance);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean moveRight(double tpf) {
        if(state != State.DEAD) {
            double newX = entity.getX() + speed * tpf;
            double newY = entity.getY();

            if (canMoveTo(newX, newY)) {
                setState(State.RIGHT);
                // Làm tròn nếu gần sát vị trí nguyên (hoặc tile)
                double snappedX = Math.round(newX);
                if (Math.abs(snappedX - newX) < 0.001) {
                    entity.setX(snappedX);
                } else {
                    entity.translateX(speed * tpf);
                }
                return true;
            } else {
                setState(State.RIGHT);
                double safeDistance = findSafeDistance(entity.getX(), entity.getY(), 1, 0, speed * tpf);
                if (safeDistance > 0) {
                    entity.translateX(safeDistance);
                    return true;
                }
            }
        }
        return false;
    }

    private double findSafeDistance(double startX, double startY, int dirX, int dirY, double maxDistance) {
        double safeDistance = 0;
        double step = 1.0;

        for (double distance = step; distance <= maxDistance; distance += step) {
            double newX = startX + dirX * distance;
            double newY = startY + dirY * distance;

            if (canMoveTo(newX, newY)) {
                safeDistance = distance;
            } else {
                break;
            }
        }

        return safeDistance;
    }

    public void stop() {
        setState(State.IDLE);
    }

    public void checkBuffCollision(List<BuffEntity> buffEntities, Pane gamePane) {
        List<BuffEntity> collectedBuffs = new ArrayList<>();

        for (BuffEntity buffEntity : buffEntities) {
            if (this.getBounds().intersects(buffEntity.getImageView().getBoundsInParent())) {
                buffEntity.getBuff().apply(this);
                collectedBuffs.add(buffEntity);
                gamePane.getChildren().remove(buffEntity.getImageView());
                gameWorld.getChildren().remove(buffEntity.getImageView());
            }
        }

        buffEntities.removeAll(collectedBuffs);
    }

    public boolean placeBomb(Pane gamePane) {
        double tileSize = GMap.TILE_SIZE;
        double snappedX = Math.floor(entity.getX() / tileSize) * tileSize;
        double snappedY = Math.floor(entity.getY() / tileSize) * tileSize;

        // 1) Không đặt hai quả bom cùng ô
        for (Pane b : bombs) {
            if (b.getLayoutX() == snappedX && b.getLayoutY() == snappedY) {
                return false;
            }
        }

        // 2) Giới hạn theo maxBombs
        if (bombCount >= maxBombs) {
            return false;
        }

        // Đặt bom mới
        bombCount++;
        Bomb bombComponent = new Bomb(this, gamePane);
        AnimatedTexture bombTexture = bombComponent.getTexture();

        // ---- DÙNG BOMB PANE ----
        BombPane bombPane = new BombPane(bombTexture, snappedX, snappedY);
        gamePane.getChildren().add(bombPane);
        gameWorld.getChildren().add(bombPane);
        bombs.add(bombPane);
        GameSceneBuilder.bombEntities.add(bombPane);

        // Âm thanh và animation đặt bom
        new AudioClip(getClass().getResource("/assets/sounds/place_bomb.wav").toString()).play();
        bombTexture.loop();

        // Hẹn 2s để nổ
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        AnimationTimer bombAnimLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                bombTexture.onUpdate(1.0 / 60.0);
            }
        };
        bombAnimLoop.start();

        delay.setOnFinished(evt -> {
            // Dừng animation bom và xoá bom khỏi scene
            bombAnimLoop.stop();
            gamePane.getChildren().remove(bombPane);
            gameWorld.getChildren().remove(bombPane);
            bombs.remove(bombPane);
            GameSceneBuilder.bombEntities.remove(bombPane);
            bombCount--;

            // Âm thanh nổ
            new AudioClip(getClass().getResource("/assets/sounds/explosion.wav").toString()).play();

            if (camera != null) {
                camera.startShake(100, 3);
            }

            // Logic nổ với kiểm tra Wall/Brick
            int bombCellX = (int)(snappedX / tileSize);
            int bombCellY = (int)(snappedY / tileSize);
            int[][] gridDirs = {
                    { 0,  0},  // trung tâm
                    { 0, -1},  // lên
                    { 0,  1},  // xuống
                    {-1,  0},  // trái
                    { 1,  0}   // phải
            };

            for (int d = 0; d < gridDirs.length; d++) {
                int dx = gridDirs[d][0];
                int dy = gridDirs[d][1];

                for (int step = 0; step <= flameRange; step++) {
                    int cellX = bombCellX + dx * step;
                    int cellY = bombCellY + dy * step;

                    // 1) Dừng hướng nếu gặp Wall
                    if (gameGMap.isWallHitbox(cellY, cellX))
                        break;

                    // 2) Vẽ flame
                    double fx = cellX * tileSize;
                    double fy = cellY * tileSize;
                    String texPath;
                    if (step == 0) texPath = "/assets/textures/central_flame.png";
                    else if (dx == 0 && dy == -1) texPath = "/assets/textures/top_up_flame.png";
                    else if (dx == 0 && dy == 1) texPath = "/assets/textures/top_down_flame.png";
                    else if (dx == -1 && dy == 0) texPath = "/assets/textures/top_left_flame.png";
                    else texPath = "/assets/textures/top_right_flame.png";

                    Image img = new Image(getClass().getResourceAsStream(texPath));
                    AnimationChannel chan = new AnimationChannel(
                            img, 3, (int)tileSize, (int)tileSize, Duration.seconds(1), 0, 2
                    );
                    AnimatedTexture flameTex = new AnimatedTexture(chan);
                    flameTex.loop();

                    Pane flamePane = new Pane(flameTex);
                    flamePane.setPrefSize(tileSize, tileSize);
                    flamePane.setLayoutX(fx);
                    flamePane.setLayoutY(fy);
                    gamePane.getChildren().add(flamePane);
                    gameWorld.getChildren().add(flamePane);
                    GameSceneBuilder.explosionEntities.add(flamePane);

                    AnimationTimer flameLoop = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            flameTex.onUpdate(1.0 / 60.0);
                        }
                    };
                    flameLoop.start();

                    // Xóa flame sau 1s
                    PauseTransition t = new PauseTransition(Duration.seconds(1));
                    t.setOnFinished(e2 -> {
                        flameLoop.stop();
                        gamePane.getChildren().remove(flamePane);
                        gameWorld.getChildren().remove(flamePane);
                        GameSceneBuilder.explosionEntities.remove(flamePane);
                    });
                    t.play();

                    // 3) Nếu gặp Brick thì phá và dừng
                    if (gameGMap.isBrickHitbox(cellY, cellX)) {
                        // Hiệu ứng phá Brick
                        Image breakSheet = new Image(
                                getClass().getResourceAsStream("/assets/textures/brick_break.png")
                        );
                        AnimationChannel breakChan = new AnimationChannel(
                                breakSheet, 3, (int)tileSize, (int)tileSize,
                                Duration.seconds(0.8), 0, 2
                        );
                        AnimatedTexture breakTex = new AnimatedTexture(breakChan);
                        breakTex.play();

                        Pane breakPane = new Pane(breakTex);
                        breakPane.setPrefSize(tileSize, tileSize);
                        breakPane.setLayoutX(cellX * tileSize);
                        breakPane.setLayoutY(cellY * tileSize);
                        gamePane.getChildren().add(breakPane);
                        gameWorld.getChildren().add(breakPane);

                        AnimationTimer breakLoop = new AnimationTimer() {
                            @Override
                            public void handle(long now) {
                                breakTex.onUpdate(1.0 / 60.0);
                            }
                        };
                        breakLoop.start();

                        PauseTransition pb = new PauseTransition(Duration.seconds(0.8));
                        pb.setOnFinished(ev3 -> {
                            breakLoop.stop();
                            gamePane.getChildren().remove(breakPane);
                            gameWorld.getChildren().remove(breakPane);

                            gameGMap.removeBrick(cellY, cellX);
                            BuffGeneric hidden = gameGMap.getHiddenBuff(cellY, cellX);
                            if (hidden != null) {
                                GameSceneBuilder.addBuffToMap(
                                        gamePane, hidden,
                                        cellX * tileSize, cellY * tileSize
                                );
                                gameGMap.clearHiddenBuff(cellY, cellX);
                            }
                        });
                        pb.play();

                        break;
                    }
                }
            }
            // Kết thúc logic nổ
        });

        delay.play();
        return true;
    }



    public void bombExploded() {
        if (bombCount > 0) {
            bombCount--;
        }
        canPlaceBomb = true;
    }

    public boolean hit() {
        // If already taking damage, prevent additional hits
        if (invincible) return false;

        lives--;
        System.out.println("Player hit! Lives remaining: " + lives);

        // Make player immediately invincible
        triggerInvincibility();

        return lives <= 0;
    }

    public void increaseFlameRange(int delta) {
        flameRange += delta;
        if (flameRange < 1) flameRange = 1;
        activeBuffs.put("flameRange", System.currentTimeMillis());
    }

    public void increaseSpeed(int delta) {
        speed = baseSpeed + delta * 100;
        activeBuffs.put("speed", System.currentTimeMillis());
    }

    public void pickUpItem(String itemType) {
        switch (itemType) {
            case "speed":
                increaseSpeed(1);
                break;
            case "flameRange":
                increaseFlameRange(1);
                break;
            case "bomb":
                setMaxBombs(getMaxBombs() + 1);
                break;
            default:
                System.out.println("Unknown item type: " + itemType);
        }
    }
    private void onExit(){
        level++; // Tăng level
        // Dừng game loop hiện tại
        if (GameSceneBuilder.gameLoop != null) {
            GameSceneBuilder.gameLoop.stop();
        }

        // Làm sạch danh sách thực thể và trạng thái
        GameSceneBuilder.buffEntities.clear();
        GameSceneBuilder.enemyEntities.clear();
        GameSceneBuilder.allEnemyEntities.clear();

        // Tạo màn chơi mới
        Scene newGameScene = GameSceneBuilder.buildNewGameScene();
        Main.mainStage.setScene(newGameScene);
        Main.mainStage.setTitle("Bomberman Game - Level: " + level);

        // Đặt lại trạng thái
        hasExited = false;

        // Đảm bảo focus vào màn chơi mới
        newGameScene.getRoot().requestFocus();
    }

    public boolean isInvincible() {
        return invincible;
    }

    public void triggerInvincibility() {
        invincible = true;

        Group view = (Group) entity.getViewComponent().getParent();
        AnimationTimer blink = new AnimationTimer() {
            private long lastToggle = 0;

            @Override
            public void handle(long now) {
                if (now - lastToggle > 100_000_000) { // 100ms
                    view.setVisible(!view.isVisible());
                    lastToggle = now;
                }
            }
        };

        blink.start();

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> {
            invincible = false;
            view.setVisible(true);
            blink.stop();
        });
        pause.play();
    }

    public void setPosition(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
        // Đẩy tọa độ xuống FXGL Entity để hiển thị
        if (entity != null) {
            entity.setPosition(x, y);
        }
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public static double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public static int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        Player.lives = lives;
    }

    public static int getMaxBombs() {
        return maxBombs;
    }

    public void setMaxBombs(int maxBombs) {
        Player.maxBombs = maxBombs;
    }

    public boolean isCanPlaceBomb() {
        return canPlaceBomb;
    }

    public void setCanPlaceBomb(boolean canPlaceBomb) {
        this.canPlaceBomb = canPlaceBomb;
    }

    public static int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        Player.level = level;
    }
    // Trả về số bom đang đặt
    public int getBombCount() {
        return this.bombCount;
    }

    // Trả về map các buff đang kích hoạt (tên → thời điểm kích hoạt)
    public Map<String, Long> getActiveBuffs() {
        return this.activeBuffs;
    }

    // Trả về danh sách Pane bom đang đặt
    public List<BombPane> getBombPanes() {
        return this.bombs;
    }
    public void setFlameRange(int r) { this.flameRange = r; }
    public void setActiveBuffs(Map<String, Long> b) { this.activeBuffs = b; }
    public boolean isFlamePassActive() {
        return flamePassActive;
    }

    public void setFlamePassActive(boolean active) {
        this.flamePassActive = active;
    }
    private void removeFlamePass() {
        flamePassActive = false;
    }
}
