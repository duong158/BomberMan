package hoyocon.bomberman.Object.EnemyGroup;

import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.EntitiesState.State;
import hoyocon.bomberman.Map.GMap;
import javafx.util.Duration;

import javafx.scene.image.Image;

import java.util.Random;

import static com.almasb.fxgl.dsl.FXGL.image;

public class Enemy extends Component {
    protected final int FRAME_SIZE = 48;
    protected static final int SCREEN_WIDTH = 1920;
    protected static final int SCREEN_HEIGHT = 1080;
    protected static final int DIRECTION_CHANGE_INTERVAL = 3000;

    protected int x,y;
    protected State state;
    protected double speed;
    protected boolean isDead = false;
    protected GMap gameMap;
    protected final Random random = new Random();
    protected long lastDirectionChangeTime;

    protected AnimatedTexture texture;
    protected AnimationChannel walkdown, walkup, walkright, walkleft, dead;

    public Enemy(int x, int y, double speed, String assetName) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.state = State.LEFT;
        Image enemyimg = new Image(getClass().getResourceAsStream(assetName));

        walkdown = new AnimationChannel(enemyimg, 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(1), 3, 5);
        walkright = new AnimationChannel(enemyimg, 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(1), 6, 8);
        walkup = new AnimationChannel(enemyimg, 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(1), 6, 8);
        walkleft = new AnimationChannel(enemyimg, 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(1), 3, 5);

        dead = new AnimationChannel(enemyimg, 6, FRAME_SIZE, FRAME_SIZE, Duration.seconds(2.4), 0, 5);

        texture = new AnimatedTexture(walkleft);
        texture.loop();

        lastDirectionChangeTime = System.currentTimeMillis();
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
        entity.getViewComponent().addChild(texture);
        entity.addComponent(new HealthIntComponent(1));
    }

    @Override
    public void onUpdate(double tpf) {
        // Make sure animations are looping properly
        if (texture != null) {
            texture.onUpdate(tpf);
        }
        
        if (isDead) return;

        // Di chuyển theo hướng hiện tại
        move(tpf);

        // Đổi hướng ngẫu nhiên theo thời gian
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDirectionChangeTime > DIRECTION_CHANGE_INTERVAL) {
            changeDirection();
            lastDirectionChangeTime = currentTime;
        }
    }

    /**
     * Đổi hướng di chuyển ngẫu nhiên.
     */
    protected void changeDirection() {
        int direction = random.nextInt(4); // 0: Lên, 1: Xuống, 2: Trái, 3: Phải
        switch (direction) {
            case 0:
                moveUp();
                break;
            case 1:
                moveDown();
                break;
            case 2:
                moveLeft();
                break;
            case 3:
                moveRight();
                break;
        }
    }

    /**
     * Di chuyển Enemy dựa trên trạng thái hiện tại.
     */
    protected void move(double tpf) {
        // Tính toán vị trí tiếp theo
        double nextX = entity.getX();
        double nextY = entity.getY();

        switch (state) {
            case UP:
                nextY -= speed * tpf;
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
    protected boolean checkCollisionWithWallOrBrick(double x, double y) {
        if (gameMap == null) {
            return false; // Không thể kiểm tra nếu không có tham chiếu đến bản đồ
        }

        // Kích thước hitbox của Enemy
        double width = 48;
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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void moveLeft() {
        state = State.LEFT;
        if (texture != null) {
            texture.loopNoOverride(walkleft);
        }
    }

    public void moveRight() {
        state = State.RIGHT;
        if (texture != null) {
            texture.loopNoOverride(walkright);
        }
    }

    public void moveUp() {
        state = State.UP;
        if (texture != null) {
            texture.loopNoOverride(walkup);
        }
    }

    public void moveDown() {
        state = State.DOWN;
        if (texture != null) {
            texture.loopNoOverride(walkdown);
        }
    }

    public void die() {
        if (!isDead) {
            isDead = true;
            texture.loopNoOverride(dead);
        }
    }
    
    public boolean isDead() {
        return isDead;
    }

}
