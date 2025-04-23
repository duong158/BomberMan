package hoyocon.bomberman.Object;

import hoyocon.bomberman.EntitiesState.State;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.util.Duration;

public class Player extends Component {
    // Vị trí người chơi
    private int x, y;
    
    // Thuộc tính người chơi
    private int lives;
    private double speed;
    private int bombCount;
    private int maxBombs;
    private boolean canPlaceBomb;
    
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
    
    public Player() {
        this.lives = 3;
        this.speed = 750;
        this.bombCount = 0;
        this.maxBombs = 1;
        this.canPlaceBomb = true;
        this.state = State.IDLE;
        this.lastAni = State.DOWN;
        
        // Khởi tạo các animation
        Image upImage = new Image(getClass().getResourceAsStream("/assets/textures/player_up.png"));
        Image downImage = new Image(getClass().getResourceAsStream("/assets/textures/player_down.png"));
        Image leftImage = new Image(getClass().getResourceAsStream("/assets/textures/player_left.png"));
        Image rightImage = new Image(getClass().getResourceAsStream("/assets/textures/player_right.png"));
        Image idleImage = new Image(getClass().getResourceAsStream("/assets/textures/player_down.png"));
        
        // Tạo các kênh animation (tham số: hình ảnh, số frame, chiều rộng, chiều cao, thời gian, frame bắt đầu, frame kết thúc)
        walkup = new AnimationChannel(upImage, 3, 45, 45, Duration.seconds(0.5), 0, 2);
        walkdown = new AnimationChannel(downImage, 3, 45, 45, Duration.seconds(0.5), 0, 2);
        walkleft = new AnimationChannel(leftImage, 3, 45, 45, Duration.seconds(0.5), 0, 2);
        walkright = new AnimationChannel(rightImage, 3, 45, 45, Duration.seconds(0.5), 0, 2);
        idledown = new AnimationChannel(idleImage, 3, 45, 45, Duration.seconds(1), 0, 0);
        
        texture = new AnimatedTexture(idledown);
    }
    
    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
    }
    
    @Override
    public void onUpdate(double tpf) {
        updateAnimation();
    }
    
    private void updateAnimation() {
        if (state == State.IDLE) {
            if (texture.getAnimationChannel() != idledown) {
                texture.loopNoOverride(idledown);
            }
        } else if (state == State.UP) {
            if (texture.getAnimationChannel() != walkup) {
                texture.loopNoOverride(walkup);
            }
            lastAni = State.UP;
        } else if (state == State.DOWN) {
            if (texture.getAnimationChannel() != walkdown) {
                texture.loopNoOverride(walkdown);
            }
            lastAni = State.DOWN;
        } else if (state == State.LEFT) {
            if (texture.getAnimationChannel() != walkleft) {
                texture.loopNoOverride(walkleft);
            }
            lastAni = State.LEFT;
        } else if (state == State.RIGHT) {
            if (texture.getAnimationChannel() != walkright) {
                texture.loopNoOverride(walkright);
            }
            lastAni = State.RIGHT;
        }
    }
    
    // Di chuyển
    public void moveUp(double tpf) {
        setState(State.UP);
        entity.translateY(-speed * tpf);
    }
    
    public void moveDown(double tpf) {
        setState(State.DOWN);
        entity.translateY(speed * tpf);
    }
    
    public void moveLeft(double tpf) {
        setState(State.LEFT);
        entity.translateX(-speed * tpf);
    }
    
    public void moveRight(double tpf) {
        setState(State.RIGHT);
        entity.translateX(speed * tpf);
    }
    
    public void stop() {
        setState(State.IDLE);
    }
    
    // Đặt bom
    public boolean placeBomb() {
        if (bombCount < maxBombs && canPlaceBomb) {
            bombCount++;
            // Logic cho việc tạo bom ở vị trí hiện tại của player
            // Có thể gửi event hoặc gọi factory để tạo bom
            
            return true;
        }
        return false;
    }
    
    // Xử lý khi bom nổ
    public void bombExploded() {
        if (bombCount > 0) {
            bombCount--;
        }
    }
    
    // Xử lý khi bị thương
    public boolean hit() {
        lives--;
        return lives <= 0;
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
