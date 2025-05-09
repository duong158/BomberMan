package hoyocon.bomberman.Object.EnemyGroup;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.GameSceneBuilder;
import hoyocon.bomberman.Map.GMap;
import hoyocon.bomberman.Object.Player;
import hoyocon.bomberman.SfxManager;
import javafx.animation.PauseTransition;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Boss extends Component {
    // Position properties
    private int x, y;

    // Animation and display
    private Map<String, String> animations = new HashMap<>();
    private ImageView currentView;
    private String currentAnim = "";
    private Player main;
    private Group gameWorld; // Reference to the game world for adding flame visuals
    private Pane gamePane;   // Reference to the game pane
    private GMap gameMap;    // Reference to the game map for collision detection

    // Combat properties
    private int health = 1;
    private boolean isAlive = true;
    private boolean isAttacking = false;
    private long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 2000; // 8 seconds cooldown
    private static final int FLAME_DAMAGE = 1; // Damage caused by flames
    private long lastHitTime = 0;


    // Attack directions (vertical and horizontal)
    private final int[][] ATTACK_DIRECTIONS = {
        {1, 0},  // Down
        {-1, 0}, // Up
        {0, 1},  // Right
        {0, -1}  // Left
    };

    // Random for choosing attack direction
    private Random random = new Random();

    /**
     * Sets the player reference for targeting
     */
    public void setMain(Player main) {
        this.main = main;
    }

    /**
     * Sets references to game world objects for flame creation
     */
    public void setGameReferences(Group gameWorld, Pane gamePane, GMap gameMap) {
        this.gameWorld = gameWorld;
        this.gamePane = gamePane;
        this.gameMap = gameMap;
    }

    /**
     * Creates a new Boss at the specified position
     * @param col column on the map
     * @param row row on the map
     */
    public Boss(int col, int row) {
        this.x = (int) (col * GMap.TILE_SIZE);
        this.y = (int) (row * GMap.TILE_SIZE);

        // Initialize ImageView
        currentView = new ImageView();
        currentView.setPreserveRatio(true);

        // Set larger size
        currentView.setFitWidth(288 * 2.5);
        currentView.setFitHeight(160 * 2.5);

        // Add GIF animations
        addAnimation("idle", "/assets/textures/bossidle.gif");
        addAnimation("attack", "/assets/textures/bossattack.gif");
        addAnimation("hit", "/assets/textures/bosshit.gif");
        addAnimation("dead", "/assets/textures/bossdie.gif");
    }

    @Override
    public void onAdded() {
        // Set position when component is added
        entity.setPosition(x, y);

        // Add view to entity
        entity.getViewComponent().addChild(currentView);

        // Default to idle animation
        playIdle();
    }

    @Override
    public void onUpdate(double tpf) {
        if (!isAlive) return;

        if (main == null) return;

        // Kiểm tra va chạm với flame
        checkCollisionWithFlame();

        // Kiểm tra va chạm với bomb
        checkCollisionWithBomb();

        // Attack logic
        long currentTime = System.currentTimeMillis();
        if (!isAttacking && currentTime - lastAttackTime > ATTACK_COOLDOWN) {
        // Ngẫu nhiên chọn giữa các đòn tấn công
            if (random.nextBoolean()) {
                attackWithRandomFlames(); // Tấn công ban đầu
            } else {
                attackWithHorizontalFlames(); // Tấn công mới với flame ngang
            }
        }
    }

    /**
     * Attack with flames at random positions
     */
    private void attackWithRandomFlames() {
        isAttacking = true;
        lastAttackTime = System.currentTimeMillis();

        // Play attack animation
        playAttack();

        // Play sound effect
        try {
            SfxManager.playExplosion();
        } catch (Exception e) {
            System.err.println("Could not play boss attack sound");
        }

        // Return to idle state after attack animation completes
        PauseTransition idleTransition = new PauseTransition(Duration.seconds(1.2));
        idleTransition.setOnFinished(e -> {
            // Create flames at random positions
            int numberOfFlames = 5; // Number of central flames
            for (int i = 0; i < numberOfFlames; i++) {
                createRandomFlameWithSpread();
            }
            isAttacking = false;
            if (isAlive) {
                playIdle();
            }
        });
        idleTransition.play();
    }

    /**
     * Create a flame at a random position with spread
     */
    private void createRandomFlameWithSpread() {
        if (gameMap == null || gamePane == null || gameWorld == null) {
            System.err.println("Cannot create flames: game references not set");
            return;
        }

        // Generate random position
        int randomRow = random.nextInt(gameMap.height);
        int randomCol = random.nextInt(gameMap.width);

        // Check if position is valid
        if (!gameMap.isWallHitbox(randomRow, randomCol)) {
            // Create central flame
            createCentralFlame(randomRow, randomCol);

            // Spread flames in all directions
            for (int[] direction : ATTACK_DIRECTIONS) {
                createFlameInDirection(randomRow, randomCol, direction[0], direction[1]);
            }
        }
    }

    /**
     * Create a central flame
     */
    private void createCentralFlame(int row, int col) {
        double tileSize = GMap.TILE_SIZE;
        double x = col * tileSize;
        double y = row * tileSize;

        String texPath = "/assets/textures/central_flame.png";
        Image img = new Image(getClass().getResourceAsStream(texPath));
        AnimationChannel chan = new AnimationChannel(
                img, 3, (int) tileSize, (int) tileSize,
                Duration.seconds(0.5), 0, 2
        );
        AnimatedTexture flameTex = new AnimatedTexture(chan);
        flameTex.loop();

        Pane flamePane = new Pane(flameTex);
        flamePane.setPrefSize(tileSize, tileSize);
        flamePane.setLayoutX(x);
        flamePane.setLayoutY(y);
        flamePane.setUserData("boss_flame");
        gamePane.getChildren().add(flamePane);
        gameWorld.getChildren().add(flamePane);
        GameSceneBuilder.explosionEntities.add(flamePane);

        // Remove flame after duration
        PauseTransition flameDuration = new PauseTransition(Duration.seconds(1));
        flameDuration.setOnFinished(e -> {
            gamePane.getChildren().remove(flamePane);
            gameWorld.getChildren().remove(flamePane);
            GameSceneBuilder.explosionEntities.remove(flamePane);
        });
        flameDuration.play();
    }

    /**
     * Create flames in a specified direction until hitting a wall
     */
    private void createFlameInDirection(int startRow, int startCol, int rowDir, int colDir) {
        createFlameSequence(startRow, startCol, rowDir, colDir, 1);
    }

    /**
     * Tạo flame theo hướng chỉ định với độ trễ giữa các bước.
     */
    private void createFlameSequence(int row, int col, int rowDir, int colDir, int step) {
        int r = row + rowDir * step;
        int c = col + colDir * step;

        // Dừng nếu ra ngoài bản đồ hoặc gặp tường
        if (r < 0 || r >= gameMap.height || c < 0 || c >= gameMap.width || gameMap.isWallHitbox(r, c)) {
            return;
        }

        // Chọn hoạt ảnh dựa trên hướng và vị trí
        String texPath;
        if (step == 1) {
            texPath = "/assets/textures/central_flame.png"; // Flame trung tâm
        } else if (rowDir == 1 && colDir == 0) {
            texPath = "/assets/textures/top_down_flame.png"; // Flame lan xuống
        } else if (rowDir == -1 && colDir == 0) {
            texPath = "/assets/textures/top_up_flame.png"; // Flame lan lên
        } else if (rowDir == 0 && colDir == 1) {
            texPath = "/assets/textures/top_right_flame.png"; // Flame lan phải
        } else {
            texPath = "/assets/textures/top_left_flame.png"; // Flame lan trái
        }

        // Tạo flame tại vị trí hiện tại
        createFlame(r, c, texPath);

        // Nếu gặp gạch, phá gạch và dừng lan tỏa
        if (gameMap.isBrickHitbox(r, c)) {
            gameMap.removeBrick(r, c);
            return;
        }

        // Tiếp tục tạo flame với độ trễ
        PauseTransition delay = new PauseTransition(Duration.millis(200)); // Độ trễ giữa các flame
        delay.setOnFinished(e -> createFlameSequence(row, col, rowDir, colDir, step + 1));
        delay.play();
    }


    private void createFlame(int row, int col, String texPath) {
        double tileSize = GMap.TILE_SIZE;
        double x = col * tileSize;
        double y = row * tileSize;

        Image img = new Image(getClass().getResourceAsStream(texPath));
        AnimationChannel chan = new AnimationChannel(
                img, 3, (int) tileSize, (int) tileSize,
                Duration.seconds(0.5), 0, 2
        );
        AnimatedTexture flameTex = new AnimatedTexture(chan);
        flameTex.loop();

        Pane flamePane = new Pane(flameTex);
        flamePane.setPrefSize(tileSize, tileSize);
        flamePane.setLayoutX(x);
        flamePane.setLayoutY(y);

        // Add a tag to mark this as a boss flame
        flamePane.setUserData("boss_flame");

        gamePane.getChildren().add(flamePane);
        gameWorld.getChildren().add(flamePane);
        GameSceneBuilder.explosionEntities.add(flamePane);

        // Remove flame after duration
        PauseTransition flameDuration = new PauseTransition(Duration.seconds(1));
        flameDuration.setOnFinished(e -> {
            gamePane.getChildren().remove(flamePane);
            gameWorld.getChildren().remove(flamePane);
            GameSceneBuilder.explosionEntities.remove(flamePane);
        });
        flameDuration.play();
    }

    // Animation methods
    public void addAnimation(String name, String gifPath) {
        animations.put(name, gifPath);
    }

    public void playAnimation(String name) {
        if (!animations.containsKey(name) || name.equals(currentAnim)) {
            return;
        }

        currentAnim = name;
        Image gifImage = new Image(getClass().getResourceAsStream(animations.get(name)));
        currentView.setImage(gifImage);
    }

    public void playIdle() {
        playAnimation("idle");
    }

    public void playAttack() {
        playAnimation("attack");
    }

    public Entity createEntity() {
        Entity bossEntity = new Entity();
        bossEntity.addComponent(this); // Thêm chính `Boss` làm component
        bossEntity.setPosition(x, y); // Đặt vị trí ban đầu của Boss
        return bossEntity;
    }

    /**
     * Kiểm tra va chạm với flame từ bomb của người chơi.
     */
    public Bounds getBounds() {
        // Make a slightly smaller hitbox for more accurate collisions
        double width = currentView.getFitWidth() * 0.15;  // Reduce width by 30%
        double height = currentView.getFitHeight() * 0.5; // Reduce height by 30%

        // Center the hitbox
        double offsetX = (currentView.getFitWidth() - width) / 2;
        double offsetY = (currentView.getFitHeight() - height) / 2;

        return new BoundingBox(
                entity.getX() + offsetX,
                entity.getY() + offsetY + 2*48,
                width,
                height
        );
    }

    private void checkCollisionWithFlame() {
        if (gamePane == null) return;

        // Use the improved hitbox calculation
        Bounds bossBounds = getBounds();

        for (Pane flamePane : GameSceneBuilder.explosionEntities) {
            Bounds flameBounds = flamePane.getBoundsInParent();
            if ("boss_flame".equals(flamePane.getUserData())) {
                continue;  // Skip this flame - boss is immune to its own flames
            }

            if (flameBounds.intersects(bossBounds)) {
                // Prevent multiple hits from the same flame within a short time
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastHitTime < 500) continue; // Use lastHitTime instead

                lastHitTime = currentTime; // Update lastHitTime, not lastAttackTime

                // Boss bị trúng flame
                health -= FLAME_DAMAGE;
                System.out.println("Boss bị trúng flame! Máu còn lại: " + health);

                if (health <= 0) {
                    die();
                } else {
                    hit();
                }
                break;
            }
        }
    }

    /**
     * Kiểm tra va chạm với bomb và dừng bomb nếu va chạm.
     */
    private void checkCollisionWithBomb() {
        if (gamePane == null) return;

        // Use the same hitbox calculation as checkCollisionWithFlame
        Bounds bossBounds = getBounds(); // Use getBounds() method for consistency

        for (Pane bombPane : GameSceneBuilder.bombEntities) {
            Bounds bombBounds = bombPane.getBoundsInParent();

            if (bombBounds.intersects(bossBounds) && bombPane instanceof Player.BombPane) {
                Player.BombPane bomb = (Player.BombPane) bombPane;
                if (bomb.isSliding()) {
                    bomb.sliding = false; // Dừng bomb khi va chạm với boss
                    System.out.println("Bomb dừng lại khi va chạm với Boss!");
                }
            }
        }
    }

    /**
     * Tấn công với hàng ngang flame lan về bên trái
     */
    private void attackWithHorizontalFlames() {
        isAttacking = true;
        lastAttackTime = System.currentTimeMillis();

        // Chơi animation tấn công
        playAttack();

        // Phát âm thanh
        try {
            SfxManager.playExplosion();
        } catch (Exception e) {
            System.err.println("Không thể phát âm thanh tấn công của boss");
        }

        // Trở lại trạng thái idle sau khi animation tấn công hoàn thành
        PauseTransition idleTransition = new PauseTransition(Duration.seconds(1.2));
        idleTransition.setOnFinished(e -> {
            // Tạo hàng dọc flame
            createVerticalFlameColumn();

            isAttacking = false;
            if (isAlive) {
                playIdle();
            }
        });
        idleTransition.play();
        GameSceneBuilder.registerPauseTransition(idleTransition);
    }

    /**
     * Tạo hàng dọc các flame cách boss một khoảng cố định
     */
    private void createVerticalFlameColumn() {
        if (gameMap == null || gamePane == null || gameWorld == null) {
            System.err.println("Không thể tạo flame: thiếu tham chiếu game");
            return;
        }

        // Tính vị trí boss theo ô
        int bossCol = (int) (entity.getX() / GMap.TILE_SIZE);

        // Khoảng cách cố định từ boss (3 ô)
        int flameCol = bossCol - 3;

        // Tạo flame theo hàng dọc với độ trễ từ trên xuống
        for (int i = 0; i < gameMap.height; i++) {
            int row = i; // Bắt đầu từ hàng trên cùng
            if (!gameMap.isWallHitbox(row, flameCol)) {
                final int finalRow = row;

                // Độ trễ để tạo hiệu ứng tuần tự từ trên xuống
                PauseTransition delay = new PauseTransition(Duration.millis(i * 100));
                delay.setOnFinished(e -> {
                    // Tạo flame trung tâm
                    createCentralFlame(finalRow, flameCol);

                    // Chỉ lan flame sang bên trái
                    createFlameInLeftDirection(finalRow, flameCol);
                });
                delay.play();
                GameSceneBuilder.registerPauseTransition(delay);
            }
        }
    }

    /**
     * Tạo flame lan sang bên trái
     */
    private void createFlameInLeftDirection(int row, int col) {
        // Hướng: trái (0, -1)
        int rowDir = 0;
        int colDir = -1;

        createFlameSequence(row, col, rowDir, colDir, 1);
    }

    /**
     * Chạy animation chết và xóa boss khỏi game.
     */
    private void die() {
        isAlive = false;
        playAnimation("dead");
        System.out.println("Boss đã chết!");

        // Hẹn xóa boss sau khi animation chết hoàn thành
        PauseTransition deathDelay = new PauseTransition(Duration.seconds(2.2));
        deathDelay.setOnFinished(e -> {
            // Xóa phần tử hiển thị từ gamePane và gameWorld
            if (entity.getViewComponent() != null && entity.getViewComponent().getParent() != null) {
                gamePane.getChildren().remove(entity.getViewComponent().getParent());
                gameWorld.getChildren().remove(entity.getViewComponent().getParent());
            }

            // Xóa tham chiếu tĩnh trong GameSceneBuilder
            if (GameSceneBuilder.boss == this) {
                GameSceneBuilder.boss = null;
            }

            // Xóa entity hoàn toàn
            entity.removeFromWorld();

            // Vô hiệu hóa các tham chiếu nội bộ
            currentView = null;
            entity = null;

            // Phát âm thanh (nếu cần)
            try {
                SfxManager.playExplosion();
            } catch (Exception ex) {
                // Bỏ qua nếu không thể phát âm thanh
            }

            System.out.println("Boss đã bị xóa hoàn toàn khỏi thế giới!");
        });
        deathDelay.play();

        // Đăng ký với GameSceneBuilder để quản lý pause/resume
        GameSceneBuilder.registerPauseTransition(deathDelay);
    }

    private void hit() {
        if (isAttacking) return; // Don't interrupt attack animation

        // Force animation reset to ensure the hit animation plays
        currentAnim = "";
        playAnimation("hit");

        // Chuyển sang trạng thái "idle" sau 1 giây
        PauseTransition transitionToIdle = new PauseTransition(Duration.seconds(1));
        transitionToIdle.setOnFinished(e -> {
            if (isAlive) playIdle();
        });
        transitionToIdle.play();

        // Add visual feedback
        currentView.setOpacity(0.7);
        PauseTransition resetOpacity = new PauseTransition(Duration.millis(200));
        resetOpacity.setOnFinished(e -> currentView.setOpacity(1.0));
        resetOpacity.play();
    }
}