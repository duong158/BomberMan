package hoyocon.bomberman.Object;

import com.almasb.fxgl.dsl.components.HealthIntComponent;
import com.almasb.fxgl.time.LocalTimer;
import hoyocon.bomberman.EntitiesState.EntityType;
import hoyocon.bomberman.EntitiesState.State;
import javafx.util.Duration;
import java.util.Random;
import static com.almasb.fxgl.dsl.FXGL.newLocalTimer;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;

/**
 * Quái vật Slime - có khả năng di chuyển ngẫu nhiên và nhảy
 */
public class Slime extends Enemy {
    public Slime(int x, int y) {
        super(x, y, 80, "/assets/textures/enemy5.png");

    }
}
