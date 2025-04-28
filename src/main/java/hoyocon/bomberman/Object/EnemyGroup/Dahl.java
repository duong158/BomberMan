package hoyocon.bomberman.Object.EnemyGroup;

import hoyocon.bomberman.Map.GMap;

import java.util.Random;

public class Dahl extends Enemy {
    private static final double MIN_SPEED = 30.0;
    private static final double MAX_SPEED = 120.0;
    
    private static final long SPEED_CHANGE_INTERVAL = 2000; // 2 giây
    
    private long lastSpeedChangeTime;
    private Random random = new Random();

    private static final double DEHL_SPEED = 50;
    public Dahl(int row, int col) {
        super(
                (int)(row * GMap.TILE_SIZE),
                (int)(col * GMap.TILE_SIZE),
                DEHL_SPEED,
                "/assets/textures/enemy4.png"
        );
    }
    @Override
    public void onUpdate(double tpf) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpeedChangeTime > SPEED_CHANGE_INTERVAL) {
            double randomSpeed = MIN_SPEED + random.nextDouble() * (MAX_SPEED - MIN_SPEED);
            setSpeed(randomSpeed);
            lastSpeedChangeTime = currentTime;
        }
        
        super.onUpdate(tpf);
    }

}