package hoyocon.bomberman.Object;

import com.almasb.fxgl.entity.component.Component;
import hoyocon.bomberman.EntitiesState.State;

import static hoyocon.bomberman.EntitiesState.State.*;
public class Player extends Component {
    private State state;
    private double speed;
    private int lives;
    private int maxBomb;
    private boolean canPlaceBomb;

    public Player() {
        this.state = IDLE;
        this.speed = 150;
        this.lives = 3;
        this.maxBomb = 1;
        this.canPlaceBomb = true;
    }


}
