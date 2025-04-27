package hoyocon.bomberman.Object;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import hoyocon.bomberman.EntitiesState.EntityType;
import hoyocon.bomberman.EntitiesState.State;
import hoyocon.bomberman.Map.GMap;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player extends Component {
    // Vị trí người chơi
    private int x, y;

    // Thuộc tính người chơi
    private int lives;
    private double speed;
    private int bombCount;
    private int maxBombs;
    private boolean canPlaceBomb;
    private final int FRAMESIZE = 45;
    private final int NUMFRAME = 3;

    // Trạng thái người chơi
    private State state;
    private State lastAni;

    // Hình ảnh và animation
    private AnimatedTexture texture;

    private AnimationChannel walkup;
    private AnimationChannel walkdown;
    private AnimationChannel walkleft;
    private AnimationChannel walkright;

    private AnimationChannel idledown;

    // Buff logic
    private boolean unlimitedBomb = false;
    private int flameRange = 1;
    private double baseSpeed = 100;

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

    // Get hitbox with margins for better collision detection
    private double[][] getHitboxPoints(double x, double y) {
        // Create points for each corner of hitbox with margin
        return new double[][] {
                // Top-left
                {x + HITBOX_MARGIN, y + HITBOX_MARGIN},
                // Top-right
                {x + PLAYER_WIDTH - HITBOX_MARGIN, y + HITBOX_MARGIN},
                // Bottom-left
                {x + HITBOX_MARGIN, y + PLAYER_HEIGHT - HITBOX_MARGIN},
                // Bottom-right
                {x + PLAYER_WIDTH - HITBOX_MARGIN, y + PLAYER_HEIGHT - HITBOX_MARGIN},
                // Middle top
                {x + PLAYER_WIDTH / 2, y + HITBOX_MARGIN},
                // Middle bottom
                {x + PLAYER_WIDTH / 2, y + PLAYER_HEIGHT - HITBOX_MARGIN},
                // Middle left
                {x + HITBOX_MARGIN, y + PLAYER_HEIGHT / 2},
                // Middle right
                {x + PLAYER_WIDTH - HITBOX_MARGIN, y + PLAYER_HEIGHT / 2}
        };
    }

    // Enhanced collision detection
    private boolean canMoveTo(double newX, double newY) {
        if (gameGMap == null) return true;

        // Check all points of player hitbox
        double[][] hitboxPoints = getHitboxPoints(newX, newY);

        for (double[] point : hitboxPoints) {
            int col = GMap.pixelToTile(point[0]);
            int row = GMap.pixelToTile(point[1]);

            // If any point collides with a non-walkable tile, movement is blocked
            if (row < 0 || row >= gameGMap.height || col < 0 || col >= gameGMap.width || !gameGMap.isWalkable(row, col)) {
                return false;
            }
        }

        // If all points pass the check, movement is allowed
        return true;
    }

    public Player() {
        this.lives = 3;
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

        walkup = new AnimationChannel(upImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(0.5), 0, 2);
        walkdown = new AnimationChannel(downImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(0.5), 0, 2);
        walkleft = new AnimationChannel(leftImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(0.5), 0, 2);
        walkright = new AnimationChannel(rightImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(0.5), 0, 2);
        idledown = new AnimationChannel(idleImage, NUMFRAME, FRAMESIZE, FRAMESIZE, Duration.seconds(1), 0, 0);

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
        updateBuffs();
    }

    private void updateAnimation() {
        switch (state) {
            case UP:
                texture.loopNoOverride(walkup);
                break;
            case RIGHT:
                texture.loopNoOverride(walkright);
                break;
            case DOWN, IDLE:
                texture.loopNoOverride(walkdown);
                break;
            case LEFT:
                texture.loopNoOverride(walkleft);
                break;
        }
    }

    private void updateBuffs() {
        long currentTime = System.currentTimeMillis();
        activeBuffs.entrySet().removeIf(entry -> {
            boolean expired = currentTime - entry.getValue() > BUFF_DURATION;
            if (expired) {
                switch (entry.getKey()) {
                    case "unlimitedBomb" -> unlimitedBomb = false;
                    case "speed" -> speed = baseSpeed;
                    // Nếu muốn reset flameRange về mặc định thì thêm ở đây
                }
            }
            return expired;
        });
    }



    // Movement với collision detection
    public boolean moveUp(double tpf) {
        // Tính toán vị trí mới
        double newX = entity.getX();
        double newY = entity.getY() - speed * tpf;

        // Kiểm tra va chạm tại vị trí mới
        if (canMoveTo(newX, newY)) {
            setState(State.UP);
            entity.translateY(-speed * tpf);
            return true;
        } else {
            setState(State.UP);
            // Tìm khoảng cách gần nhất có thể di chuyển được
            double safeDistance = findSafeDistance(entity.getX(), entity.getY(), 0, -1, speed * tpf);
            if (safeDistance > 0) {
                entity.translateY(-safeDistance);
                return true;
            }
        }
        return false;
    }

    public boolean moveDown(double tpf) {
        // Tính toán vị trí mới
        double newX = entity.getX();
        double newY = entity.getY() + speed * tpf;

        // Kiểm tra va chạm tại vị trí mới
        if (canMoveTo(newX, newY)) {
            setState(State.DOWN);
            entity.translateY(speed * tpf);
            return true;
        } else {
            setState(State.DOWN);
            // Tìm khoảng cách gần nhất có thể di chuyển được
            double safeDistance = findSafeDistance(entity.getX(), entity.getY(), 0, 1, speed * tpf);
            if (safeDistance > 0) {
                entity.translateY(safeDistance);
                return true;
            }
        }
        return false;
    }

    public boolean moveLeft(double tpf) {
        // Tính toán vị trí mới
        double newX = entity.getX() - speed * tpf;
        double newY = entity.getY();

        // Kiểm tra va chạm tại vị trí mới
        if (canMoveTo(newX, newY)) {
            setState(State.LEFT);
            entity.translateX(-speed * tpf);
            return true;
        } else {
            setState(State.LEFT);
            // Tìm khoảng cách gần nhất có thể di chuyển được
            double safeDistance = findSafeDistance(entity.getX(), entity.getY(), -1, 0, speed * tpf);
            if (safeDistance > 0) {
                entity.translateX(-safeDistance);
                return true;
            }
        }
        return false;
    }

    public boolean moveRight(double tpf) {
        // Tính toán vị trí mới
        double newX = entity.getX() + speed * tpf;
        double newY = entity.getY();

        // Kiểm tra va chạm tại vị trí mới
        if (canMoveTo(newX, newY)) {
            setState(State.RIGHT);
            entity.translateX(speed * tpf);
            return true;
        } else {
            setState(State.RIGHT);
            // Tìm khoảng cách gần nhất có thể di chuyển được
            double safeDistance = findSafeDistance(entity.getX(), entity.getY(), 1, 0, speed * tpf);
            if (safeDistance > 0) {
                entity.translateX(safeDistance);
                return true;
            }
        }
        return false;
    }

    // Phương thức tìm khoảng cách an toàn có thể di chuyển
    private double findSafeDistance(double startX, double startY, int dirX, int dirY, double maxDistance) {
        // Tìm khoảng cách an toàn lớn nhất có thể di chuyển
        double safeDistance = 0;
        double step = 1.0; // Bước nhỏ để tìm kiếm

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

    // Buff collision handling
    public void checkBuffCollision(List<BuffEntity> buffEntities, Pane gamePane) {
        List<BuffEntity> collectedBuffs = new ArrayList<>();

        for (BuffEntity buffEntity : buffEntities) {
            if (this.getBounds().intersects(buffEntity.getImageView().getBoundsInParent())) {
                // Apply buff to player
                buffEntity.getBuff().apply(this);

                // Add to list of collected buffs
                collectedBuffs.add(buffEntity);

                // Remove buff image from gamePane
                gamePane.getChildren().remove(buffEntity.getImageView());
            }
        }

        // Remove collected buffs from buffEntities list
        buffEntities.removeAll(collectedBuffs);
    }


    // Đặt bom
    public boolean placeBomb(Pane gamePane) {
        if ((bombCount < maxBombs || unlimitedBomb) && canPlaceBomb) {
            bombCount++;
            canPlaceBomb = false;

            double tileSize = GMap.TILE_SIZE;
            double snappedX = Math.floor(getEntity().getX() / tileSize) * tileSize;
            double snappedY = Math.floor(getEntity().getY() / tileSize) * tileSize;

            // Tạo texture cho bom và thêm vào Pane
            Bomb bombComponent = new Bomb(this);
            AnimatedTexture bombTexture = bombComponent.getTexture();
            Pane bombPane = new Pane();
            bombPane.setPrefSize(tileSize, tileSize);
            bombPane.getChildren().add(bombTexture);
            bombPane.setLayoutX(snappedX);
            bombPane.setLayoutY(snappedY);
            gamePane.getChildren().add(bombPane);

            // Bắt đầu hoạt ảnh
            bombTexture.play();

            // Hẹn giờ nổ sau 3 giây
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(evt -> {
                // Xóa bom khỏi gamePane và thông báo nổ
                System.out.println("Bomb exploded!");
                gamePane.getChildren().remove(bombPane);
                this.bombExploded();
            });
            delay.play();
            AnimationTimer bombAnimLoop = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    bombTexture.onUpdate(1.0/60.0);   // hoặc truyền đúng tpf
                }
            };
            bombAnimLoop.start();

            // khi bom nổ, dừng luôn timer:
            delay.setOnFinished(evt -> {
                bombAnimLoop.stop();
                gamePane.getChildren().remove(bombPane);
                this.bombExploded();
            });

            return true;
        }
        return false;
    }

    // Xử lý khi bom nổ
    public void bombExploded() {
        if (bombCount > 0) {
            bombCount--;
        }
        canPlaceBomb = true;
    }

    // Xử lý khi bị thương
    public boolean hit() {
        lives--;
        return lives <= 0;
    }

    // Buff logic
    public void setUnlimitedBomb(boolean value) {
        unlimitedBomb = value;
        if (value) {
            activeBuffs.put("unlimitedBomb", System.currentTimeMillis());
        }
    }

    public boolean isUnlimitedBomb() {
        return unlimitedBomb;
    }

    public void increaseFlameRange(int delta) {
        flameRange += delta;
        if (flameRange < 1) flameRange = 1;
        activeBuffs.put("flameRange", System.currentTimeMillis());
    }

    public void increaseSpeed(int delta) {
        speed = baseSpeed + delta * 50;
        activeBuffs.put("speed", System.currentTimeMillis());
    }

    // Logic khi nhặt vật phẩm
    public void pickUpItem(String itemType) {
        switch (itemType) {
            case "speed":
                increaseSpeed(1);
                break;
            case "flameRange":
                increaseFlameRange(1);
                break;
            case "unlimitedBomb":
                setUnlimitedBomb(true);
                break;
            default:
                System.out.println("Unknown item type: " + itemType);
        }
    }


    // Getters và Setters
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getMaxBombs() {
        return maxBombs;
    }

    public void setMaxBombs(int maxBombs) {
        this.maxBombs = maxBombs;
    }

    public boolean isCanPlaceBomb() {
        return canPlaceBomb;
    }

    public void setCanPlaceBomb(boolean canPlaceBomb) {
        this.canPlaceBomb = canPlaceBomb;
    }
}
