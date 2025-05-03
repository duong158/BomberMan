package hoyocon.bomberman.Save;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {
    // Player
    public double playerX, playerY;
    public int lives, bombCount, maxBombs, flameRange;
    public double speed;
    public Map<String, Long> activeBuffs;

    // Enemies: danh sách các trạng thái kẻ địch
    public List<EnemyState> enemies;

    // Map
    public int[][] mapData;

    // Buffs trên bản đồ
    public List<BuffState> buffs;

    // Bombs đang đặt
    public List<BombState> bombs;
}