package hoyocon.bomberman.Object.EnemyGroup;

import hoyocon.bomberman.Map.GMap;

/**
 * Quái vật Slime - có khả năng di chuyển ngẫu nhiên và nhảy
 */
public class Oneal extends Enemy {
    private static final double ONEAL_SPEED = 80;
    public Oneal(int col, int row) {
        super(
                (int)(col * GMap.TILE_SIZE),
                (int)(row * GMap.TILE_SIZE),
                ONEAL_SPEED,
                "/assets/textures/enemy2.png"
        );
    }
}
