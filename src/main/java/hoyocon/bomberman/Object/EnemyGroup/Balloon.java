package hoyocon.bomberman.Object.EnemyGroup;

import com.almasb.fxgl.dsl.components.HealthIntComponent;
import hoyocon.bomberman.EntitiesState.State;
import hoyocon.bomberman.Map.GMap;
import java.util.Random;

/**
 * Quái vật Balloon - di chuyển ngẫu nhiên với tốc độ chậm.
 */
public class Balloon extends Enemy {
    // Hằng số
    private static final double BALLOON_SPEED = 40;

    /**
     * Khởi tạo Balloon mới tại vị trí x, y.
     */
    public Balloon(int row, int col) {
        // Convert tile coordinates to pixel coordinates
        super(
                (int)(row * GMap.TILE_SIZE),
                (int)(col * GMap.TILE_SIZE),
                BALLOON_SPEED,
                "/assets/textures/enemy1.png"
        );
    }
}