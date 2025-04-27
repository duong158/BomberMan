package hoyocon.bomberman.Object.EnemyGroup;

import com.almasb.fxgl.dsl.components.HealthIntComponent;
import hoyocon.bomberman.EntitiesState.State;
import hoyocon.bomberman.Map.GMap;
import java.util.Random;

/**
 * Quái vật Balloon - di chuyển ngẫu nhiên với tốc độ chậm.
 */
public class Pass extends Enemy {
    // Hằng số
    private static final double PASS_SPEED = 80; // Tốc độ chậm hơn
    private static final int DIRECTION_CHANGE_INTERVAL = 3000; // Đổi hướng sau mỗi 3 giây (ms)
    private static final int SCREEN_WIDTH = 1920;     // Chiều rộng màn hình
    private static final int SCREEN_HEIGHT = 1080;    // Chiều cao màn hình

    // Thuộc tính
    private final Random random = new Random();       // Bộ sinh số ngẫu nhiên
    private boolean isDead = false;                   // Trạng thái chết

    // Replace FXGL timer with simple timestamp approach
    private long lastDirectionChangeTime;

    // Add reference to game map
    private GMap gameMap;

    /**
     * Khởi tạo Balloon mới tại vị trí x, y.
     */
    public Pass(int col, int row) {
        // Convert tile coordinates to pixel coordinates
        super(
                (int)(col * GMap.TILE_SIZE),
                (int)(row * GMap.TILE_SIZE),
                PASS_SPEED,
                "/assets/textures/enemy3.png"
        );

        // Initialize with current timestamp instead of FXGL timer
        lastDirectionChangeTime = System.currentTimeMillis();

        // Chọn hướng di chuyển ngẫu nhiên ban đầu
        changeDirection();
    }

    /**
     * Set the game map reference for collision detection
     */
    public void setGameMap(GMap gameMap) {
        this.gameMap = gameMap;
    }

    @Override
    public void onAdded() {
        super.onAdded(); // Gọi phương thức của lớp cha để thêm texture

        // Thiết lập máu (ví dụ: 1 máu)
        entity.addComponent(new HealthIntComponent(1));
    }

    @Override
    public void onUpdate(double tpf) {
        // Không làm gì nếu đã chết
        if (isDead) return;

        // Di chuyển theo hướng hiện tại
        move(tpf);

        // Đổi hướng ngẫu nhiên theo thời gian - use elapsed time instead of FXGL timer
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDirectionChangeTime > DIRECTION_CHANGE_INTERVAL) {
            changeDirection();
            lastDirectionChangeTime = currentTime;
        }

    }

    /**
     * Đổi hướng di chuyển ngẫu nhiên.
     */
    private void changeDirection() {
        int direction = random.nextInt(4); // 0: Lên, 1: Xuống, 2: Trái, 3: Phải
        switch (direction) {
            case 0:
                moveUp(); // Gọi phương thức từ lớp Enemy
                break;
            case 1:
                moveDown(); // Gọi phương thức từ lớp Enemy
                break;
            case 2:
                moveLeft(); // Gọi phương thức từ lớp Enemy
                break;
            case 3:
                moveRight(); // Gọi phương thức từ lớp Enemy
                break;
        }
    }

    /**
     * Di chuyển Balloon dựa trên trạng thái hiện tại.
     */
    private void move(double tpf) {
        // Tính toán vị trí tiếp theo
        double nextX = entity.getX();
        double nextY = entity.getY();

        switch (state) { // state được kế thừa từ Enemy
            case UP:
                nextY -= speed * tpf; // speed được kế thừa từ Enemy
                break;
            case DOWN:
                nextY += speed * tpf;
                break;
            case LEFT:
                nextX -= speed * tpf;
                break;
            case RIGHT:
                nextX += speed * tpf;
                break;
            default:
                // Trạng thái không xác định, có thể đứng yên hoặc đổi hướng
                changeDirection();
                return; // Không di chuyển trong frame này
        }

        // Kiểm tra va chạm với tường hoặc gạch
        if (checkCollisionWithWallOrBrick(nextX, nextY)) {
            handleCollision();
        } else {
            // Nếu không va chạm, cập nhật vị trí
            entity.setPosition(nextX, nextY);
        }
    }

    /**
     * Kiểm tra va chạm với tường hoặc gạch bằng hitbox.
     */
    private boolean checkCollisionWithWallOrBrick(double x, double y) {
        if (gameMap == null) {
            return false; // Không thể kiểm tra nếu không có tham chiếu đến bản đồ
        }

        // Kích thước hitbox của Balloon
        double width = 48; // Giả sử kích thước Balloon là 48x48
        double height = 48;

        // Kiểm tra va chạm với tường hoặc gạch
        return gameMap.checkCollisionWithWall(x, y, width, height) ||
                gameMap.checkCollisionWithBrick(x, y, width, height);
    }

    /**
     * Xử lý va chạm với tường hoặc gạch.
     */
    public void handleCollision() {
        // Đổi hướng di chuyển ngẫu nhiên khi va chạm với tường hoặc gạch
        changeDirection();

        // Reset thời gian đổi hướng để tránh đổi hướng liên tục
        lastDirectionChangeTime = System.currentTimeMillis();
    }




    /**
     * Xử lý khi Balloon bị tiêu diệt.
     * Ghi đè phương thức die() từ lớp Enemy.
     */
    @Override
    public void die() {
        if (!isDead) {
            isDead = true;
            // Gọi phương thức die của lớp cha để chạy animation chết (nếu có)
            super.die();

            // Nếu lớp cha không xử lý xóa entity, bạn cần thêm code ở đây
            // Ví dụ:
            // getGameWorld().runOnceAfter(() -> {
            //     entity.removeFromWorld();
            // }, Duration.seconds(1)); // Thời gian chờ animation chết
        }
    }

    /**
     * Kiểm tra trạng thái chết.
     */
    public boolean isDead() {
        return  isDead;
    }
}