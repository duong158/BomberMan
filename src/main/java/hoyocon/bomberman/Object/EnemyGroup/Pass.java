package hoyocon.bomberman.Object.EnemyGroup;

import hoyocon.bomberman.Map.GMap;

/**
 * Quái vật Pass - di chuyển ngẫu nhiên với tốc độ nhanh hơn.
 */
public class Pass extends Enemy {
    // Hằng số
    private static final double PASS_SPEED = 80; // Tốc độ nhanh hơn

    /**
     * Khởi tạo Pass mới tại vị trí x, y.
     */
    public Pass(int row, int col) {
        // Convert tile coordinates to pixel coordinates
        super(
                (int)(row * GMap.TILE_SIZE),
                (int)(col * GMap.TILE_SIZE),
                PASS_SPEED,
                "/assets/textures/enemy3.png"
        );
    }
}
