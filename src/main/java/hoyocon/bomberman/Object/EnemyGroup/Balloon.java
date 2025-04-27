package hoyocon.bomberman.Object.EnemyGroup;

import hoyocon.bomberman.Map.GMap;

/**
 * Quái vật Balloon - di chuyển ngẫu nhiên với tốc độ chậm.
 */
public class Balloon extends Enemy {
    // Hằng số
    private static final double BALLOON_SPEED = 40;

    /**
     * Khởi tạo Balloon mới tại vị trí x, y.
     */
    public Balloon(int col, int row) {
        // Convert tile coordinates to pixel coordinates
        super(
                (int)(col * GMap.TILE_SIZE),
                (int)(row * GMap.TILE_SIZE),
                BALLOON_SPEED,
                "/assets/textures/enemy1.png"
        );
    }
}