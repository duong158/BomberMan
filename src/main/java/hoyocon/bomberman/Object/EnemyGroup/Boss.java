package hoyocon.bomberman.Object.EnemyGroup;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import hoyocon.bomberman.EntitiesState.EntityType; // Thay thế BomberManType
import hoyocon.bomberman.Map.GMap;
import hoyocon.bomberman.Object.Player;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class Boss extends Component {
    // Thuộc tính vị trí
    private int x, y;

    // Animation và hiển thị
    private Map<String, String> animations = new HashMap<>();
    private ImageView currentView;
    private String currentAnim = "";

    // Thuộc tính chiến đấu
    private int health = 100;
    private boolean isAlive = true;
    private boolean isAttacking = false;
    private long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 3000; // 3 giây
    private static final double ATTACK_RANGE = 250; // Phạm vi tấn công

    /**
     * Tạo một Boss mới tại vị trí chỉ định
     * @param col cột trên bản đồ
     * @param row hàng trên bản đồ
     */
    public Boss(int col, int row) {
        this.x = (int) (col * GMap.TILE_SIZE);
        this.y = (int) (row * GMap.TILE_SIZE);

        // Khởi tạo ImageView
        currentView = new ImageView();
        currentView.setPreserveRatio(true);

        // Thiết lập kích thước lớn hơn (gấp đôi)
        currentView.setFitWidth(288 * 2.5);
        currentView.setFitHeight(160 * 2.5);

        // Thêm các animation GIF
        addAnimation("idle", "/assets/textures/bossidle.gif");
        addAnimation("attack", "/assets/textures/bossattack.gif");
        addAnimation("hit", "/assets/textures/bosshit.gif");
        addAnimation("dead", "/assets/textures/bossdie.gif");
    }

    @Override
    public void onAdded() {
        // Đặt vị trí của entity khi component được thêm vào
        entity.setPosition(x, y);

        // Thêm view vào entity
        entity.getViewComponent().addChild(currentView);

        // Mặc định hiển thị animation idle
        playIdle();
    }

    @Override
    public void onUpdate(double tpf) {
        // Logic cập nhật Boss
        if (!isAlive) return;

        // Tìm player trong gameworld
        try {
            // Sửa từ BomberManType.PLAYER thành EntityType.PLAYER
            Entity player = getEntity().getWorld().getSingleton(EntityType.PLAYER);
            if (player != null) {
                // Tính khoảng cách đến player
                double distance = entity.getPosition().distance(player.getPosition());

                // Logic tấn công khi player đến gần
                long currentTime = System.currentTimeMillis();
                if (distance < ATTACK_RANGE && !isAttacking && currentTime - lastAttackTime > ATTACK_COOLDOWN) {
                    attack(player);
                }
            }
        } catch (Exception e) {
            // Xử lý trường hợp không tìm thấy player
        }
    }

    /**
     * Thực hiện tấn công người chơi
     * @param player entity của người chơi
     */
    private void attack(Entity player) {
        isAttacking = true;
        lastAttackTime = System.currentTimeMillis();

        // Chuyển hướng nhìn của boss về phía player
        if (player.getX() > entity.getX()) {
            currentView.setScaleX(2.0); // Nhìn sang phải, giữ kích thước phóng to 2 lần
        } else {
            currentView.setScaleX(-2.0); // Nhìn sang trái, giữ kích thước phóng to 2 lần
        }

        // Chơi animation tấn công
        playAttack();

        // Gây sát thương cho player nếu đang trong tầm
        try {
            if (player.hasComponent(Player.class)) {
                Player playerComponent = player.getComponent(Player.class);
                playerComponent.hit();
            }
        } catch (Exception e) {
            // Xử lý lỗi
        }

        // Sau khi tấn công xong, quay lại trạng thái idle
        FXGL.runOnce(() -> {
            isAttacking = false;
            if (isAlive) {
                playIdle();
            }
        }, Duration.seconds(1.5));
    }

    /**
     * Thêm animation mới cho boss
     * @param name tên animation
     * @param gifPath đường dẫn đến file GIF
     */
    public void addAnimation(String name, String gifPath) {
        animations.put(name, gifPath);
    }

    /**
     * Chơi animation theo tên
     * @param name tên animation
     */
    public void playAnimation(String name) {
        if (!animations.containsKey(name) || name.equals(currentAnim)) {
            return;
        }

        currentAnim = name;
        Image gifImage = new Image(getClass().getResourceAsStream(animations.get(name)));
        currentView.setImage(gifImage);
    }

    /**
     * Chơi animation idle (đứng yên)
     */
    public void playIdle() {
        playAnimation("idle");
    }

    /**
     * Chơi animation tấn công
     */
    public void playAttack() {
        playAnimation("attack");
    }

    /**
     * Chơi animation bị đánh
     */
    public void playHit() {
        playAnimation("hit");

        // Trở về idle sau khi hoàn thành animation hit
        FXGL.runOnce(() -> {
            if (isAlive) {
                playIdle();
            }
        }, Duration.seconds(0.5));
    }

    /**
     * Chơi animation chết
     */
    public void playDead() {
        playAnimation("dead");
    }

    /**
     * Xử lý khi boss nhận sát thương
     * @param damage lượng sát thương nhận vào
     */
    public void takeDamage(int damage) {
        if (!isAlive) return;

        health -= damage;
        System.out.println("Boss took damage! Health: " + health);
        playHit();

        if (health <= 0) {
            health = 0;
            die();
        }
    }

    /**
     * Xử lý khi boss chết
     */
    private void die() {
        isAlive = false;
        playDead();

        // Xóa boss sau khi animation chết kết thúc
        FXGL.runOnce(() -> {
            entity.removeFromWorld();
            System.out.println("Boss has been defeated!");
        }, Duration.seconds(2));
    }

    /**
     * Tạo entity boss từ component này
     * @return Entity đã được cấu hình
     */
    public Entity createEntity() {
        Entity bossEntity = FXGL.entityBuilder()
                .type(EntityType.ENEMY) // Sửa từ BomberManType.BOSS thành EntityType.ENEMY
                .at(x, y)
                .with(this)
                .collidable()
                .build();

        return bossEntity;
    }

    // Getters
    public int getHealth() {
        return health;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}