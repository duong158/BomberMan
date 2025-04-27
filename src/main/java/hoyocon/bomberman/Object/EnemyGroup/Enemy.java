package hoyocon.bomberman.Object.EnemyGroup;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.EntitiesState.State;
import javafx.util.Duration;

import javafx.scene.image.Image;

import static com.almasb.fxgl.dsl.FXGL.image;

public class Enemy extends Component {
    private final int FRAME_SIZE = 48;

    protected int x,y;
    protected State state;
    protected double speed;

    protected AnimatedTexture texture;
    protected AnimationChannel walkdown, walkup, walkright, walkleft, dead;

    public Enemy(int x, int y, double speed, String assetName) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.state = State.LEFT;
        Image enemyimg = new Image(getClass().getResourceAsStream(assetName));

        walkdown = new AnimationChannel(enemyimg, 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 3, 5);
        walkright = new AnimationChannel(enemyimg, 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 6, 8);
        walkup = new AnimationChannel(enemyimg, 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 6, 8);
        walkleft = new AnimationChannel(enemyimg, 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 3, 5);

        dead = new AnimationChannel(enemyimg, 6, FRAME_SIZE, FRAME_SIZE, Duration.seconds(2.4), 0, 5);

        texture = new AnimatedTexture(walkleft);
        texture.loop();


    }
    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
    }

    @Override
    public void onUpdate(double tpf) {
        // Make sure animations are looping properly
        if (texture != null) {
            texture.onUpdate(tpf);
        }
        // Add other update logic here
    }

    public void moveLeft() {
        state = State.LEFT;
        if (texture != null) {
            texture.playAnimationChannel(walkleft);
        }
    }

    public void moveRight() {
        state = State.RIGHT;
        if (texture != null) {
            texture.playAnimationChannel(walkright);
        }
    }

    public void moveUp() {
        state = State.UP;
        if (texture != null) {
            texture.playAnimationChannel(walkup);
        }
    }

    public void moveDown() {
        state = State.DOWN;
        if (texture != null) {
            texture.playAnimationChannel(walkdown);
        }
    }

    public void die() {
        texture.playAnimationChannel(dead);
    }

}
