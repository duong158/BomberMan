package hoyocon.bomberman.Object;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.EntitiesState.State;
import javafx.animation.Animation;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.*;


import static hoyocon.bomberman.EntitiesState.State.*;
public class Player extends Component {
    private final int FRAME_SIZE = 45;
    private State state;
    private double speed;
    private int lives;
    private int maxBomb;
    private boolean canPlaceBomb;
    private AnimationChannel walkdown;
    private AnimationChannel walkup;
    private AnimationChannel walkleft;
    private AnimationChannel walkright;
    private AnimationChannel idleright;
    private AnimationChannel idleleft;
    private AnimationChannel idleup;
    private AnimationChannel idledown;
    private AnimationChannel dead;

    private AnimatedTexture texture;

    private State lastAni = IDLE;

    public Player() {
        this.state = IDLE;
        this.speed = 150;
        this.lives = 3;
        this.maxBomb = 1;
        this.canPlaceBomb = true;

        dead = new AnimationChannel(image("player_die.png"), 5, FRAME_SIZE, FRAME_SIZE, Duration.seconds(3.5), 0, 4);

        idledown = new AnimationChannel(image("player_down.png"), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 0, 0);
        idleright = new AnimationChannel(image("player_right.png"), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 0, 0);
        idleup = new AnimationChannel(image("player_up.png"), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 0, 0);
        idleleft = new AnimationChannel(image("player_left.png"), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 0, 0);

        walkdown = new AnimationChannel(image("player_down.png"), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 0, 2);
        walkright = new AnimationChannel(image("player_right.png"), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 0, 2);
        walkup = new AnimationChannel(image("player_up.png"), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 0, 2);
        walkleft = new AnimationChannel(image("player_left.png"), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 0, 2);

        texture = new AnimatedTexture(idledown);
        texture.loop();
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

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public int getMaxBomb() {
        return maxBomb;
    }

    public void setMaxBomb(int maxBomb) {
        this.maxBomb = maxBomb;
    }

    public boolean isCanPlaceBomb() {
        return canPlaceBomb;
    }

    public void setCanPlaceBomb(boolean canPlaceBomb) {
        this.canPlaceBomb = canPlaceBomb;
    }
    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        // Cập nhật animation dựa trên trạng thái người chơi
        updateAnimation();
    }

    private void updateAnimation() {
        if (state == IDLE) {
            if (texture.getAnimationChannel() != idledown) {
                texture.loopAnimationChannel(idledown);
            }
        } else if (state == MOVING_UP) {
            if (texture.getAnimationChannel() != animWalkUp) {
                texture.loopAnimationChannel(animWalkUp);
            }
            lastDirection = MOVING_UP;
        } else if (state == MOVING_DOWN) {
            if (texture.getAnimationChannel() != animWalkDown) {
                texture.loopAnimationChannel(animWalkDown);
            }
            lastDirection = MOVING_DOWN;
        } else if (state == MOVING_LEFT) {
            if (texture.getAnimationChannel() != animWalkLeft) {
                texture.loopAnimationChannel(animWalkLeft);
            }
            lastDirection = MOVING_LEFT;
        } else if (state == MOVING_RIGHT) {
            if (texture.getAnimationChannel() != animWalkRight) {
                texture.loopAnimationChannel(animWalkRight);
            }
            lastDirection = MOVING_RIGHT;
        }
    }



}
