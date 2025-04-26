package hoyocon.bomberman.Object;

import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.LocalTimer;
import hoyocon.bomberman.EntitiesState.EntityType; // Assuming you might add this later
import hoyocon.bomberman.EntitiesState.State;
import javafx.util.Duration;
import java.util.Random;
import static com.almasb.fxgl.dsl.FXGL.newLocalTimer;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;

/**
 * Quái vật Balloon - di chuyển ngẫu nhiên với tốc độ chậm.
 */
public class Balloon extends Enemy {
    // Hằng số
    private static final double BALLOON_SPEED = 40; // Tốc độ chậm hơn
    private static final int DIRECTION_CHANGE_INTERVAL = 3; // Đổi hướng sau mỗi 3 giây
    private static final int SCREEN_WIDTH = 1080;     // Chiều rộng màn hình (cần điều chỉnh)
    private static final int SCREEN_HEIGHT = 1920;    // Chiều cao màn hình (cần điều chỉnh)

    // Thuộc tính
    private final LocalTimer movementTimer; // Hẹn giờ đổi hướng
    private final Random random;            // Bộ sinh số ngẫu nhiên
    private boolean isDead = false;         // Trạng thái chết

    /**
     * Khởi tạo Balloon mới tại vị trí x, y.
     */
    public Balloon(int x, int y) {
        // Gọi constructor của lớp cha với tốc độ chậm và hình ảnh balloon
        super(x, y, BALLOON_SPEED, "/assets/textures/enemy1.png"); // Sử dụng asset enemy1.png

        // Khởi tạo timer và random
        movementTimer = newLocalTimer();
        movementTimer.capture();
        random = new Random();

        // Chọn hướng di chuyển ngẫu nhiên ban đầu
        changeDirection();
    }

    @Override
    public void onAdded() {
        super.onAdded(); // Gọi phương thức của lớp cha để thêm texture

        // Thiết lập máu (ví dụ: 1 máu)
        entity.addComponent(new HealthIntComponent(1));

        // Nếu bạn sử dụng EntityType, hãy thêm dòng này:
        // entity.setType(EntityType.ENEMY);
    }

    @Override
    public void onUpdate(double tpf) {
        // Không làm gì nếu đã chết
        if (isDead) return;

        // Di chuyển theo hướng hiện tại
        move(tpf);

        // Đổi hướng ngẫu nhiên theo thời gian
        if (movementTimer.elapsed(Duration.seconds(DIRECTION_CHANGE_INTERVAL))) {
            changeDirection();
            movementTimer.capture();
        }

        // Kiểm tra va chạm với biên màn hình
        checkBoundaryCollisions();
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

        // Cập nhật vị trí nếu hợp lệ (không ra khỏi biên)
        // Việc kiểm tra va chạm với tường/gạch nên được xử lý bởi hệ thống va chạm của FXGL
        if (isValidMove(nextX, nextY)) {
            entity.setPosition(nextX, nextY);
        } else {
            // Nếu di chuyển không hợp lệ (ra khỏi biên), đổi hướng
            changeDirection();
        }
    }

     /**
     * Kiểm tra vị trí mới có nằm trong giới hạn màn hình không.
     */
    private boolean isValidMove(double x, double y) {
        // Sử dụng FRAME_SIZE từ lớp Enemy (nếu có) hoặc kích thước cố định
        int frameSize = 48; // Giả sử kích thước frame là 48x48
        return x >= 0 && x <= SCREEN_WIDTH - frameSize &&
               y >= 0 && y <= SCREEN_HEIGHT - frameSize;
    }

    /**
     * Kiểm tra và xử lý va chạm với biên màn hình.
     */
    private void checkBoundaryCollisions() {
        double x = entity.getX();
        double y = entity.getY();
        int frameSize = 48; // Giả sử kích thước frame là 48x48
        boolean needsDirectionChange = false;

        // Kiểm tra biên trái/phải
        if (x < 0) {
            entity.setX(0);
            needsDirectionChange = true;
        } else if (x > SCREEN_WIDTH - frameSize) {
            entity.setX(SCREEN_WIDTH - frameSize);
            needsDirectionChange = true;
        }

        // Kiểm tra biên trên/dưới
        if (y < 0) {
            entity.setY(0);
            needsDirectionChange = true;
        } else if (y > SCREEN_HEIGHT - frameSize) {
            entity.setY(SCREEN_HEIGHT - frameSize);
            needsDirectionChange = true;
        }

        // Đổi hướng nếu chạm biên
        if (needsDirectionChange) {
            changeDirection();
        }
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
        return isDead;
    }
}