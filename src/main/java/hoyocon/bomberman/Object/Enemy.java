package hoyocon.bomberman.Object;

import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.EntitiesState.State;
import javafx.util.Duration;

import java.awt.*;

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

        walkdown = new AnimationChannel(image(assetName), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 3, 5);
        walkright = new AnimationChannel(image(assetName), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 6, 8);
        walkup = new AnimationChannel(image(assetName), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 6, 8);
        walkleft = new AnimationChannel(image(assetName), 3, FRAME_SIZE, FRAME_SIZE, Duration.seconds(0.5), 3, 5);

        texture = new AnimatedTexture(walkleft);
        texture.loop();
    }

}
