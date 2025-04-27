package hoyocon.bomberman.Object.EnemyGroup;

import hoyocon.bomberman.EntitiesState.State;
import hoyocon.bomberman.Map.GMap;
import hoyocon.bomberman.Object.Player;

import java.util.*;

/**
 * Quái vật Oneal - có khả năng di chuyển ngẫu nhiên và đuổi theo người chơi
 * khi người chơi vào phạm vi xác định
 */
public class Oneal extends Enemy {
    private static final double ONEAL_NORMAL_SPEED = 80;
    private static final double ONEAL_CHASE_SPEED = 120;
    private static final int DETECTION_RANGE = 5;
    private static final double STUCK_THRESHOLD = 0.5; // giây
    private static final double MOVE_EPSILON = 1.0; // độ thay đổi tối thiểu để tính là di chuyển

    private double directionChangeTimer = 0;
    private double stuckTimer = 0;
    private double lastX, lastY;

    private Player player;
    private boolean isChasing = false;

    public Oneal(int col, int row) {
        super(
                (int)(col * GMap.TILE_SIZE),
                (int)(row * GMap.TILE_SIZE),
                ONEAL_NORMAL_SPEED,
                "/assets/textures/enemy2.png"
        );
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    private boolean isPlayerInRange(int playerRow, int playerCol, int onealRow, int onealCol) {
        int distance = Math.abs(playerRow - onealRow) + Math.abs(playerCol - onealCol);
        return distance <= DETECTION_RANGE;
    }

    @Override
    public void onUpdate(double tpf) {
        this.x = (int) entity.getX();
        this.y = (int) entity.getY();

        if (isDead) return;

        if (player == null || gameMap == null) {
            super.onUpdate(tpf);
            return;
        }

        int playerRow = GMap.pixelToTile(player.getEntity().getY());
        int playerCol = GMap.pixelToTile(player.getEntity().getX());
        int onealRow = GMap.pixelToTile(entity.getY());
        int onealCol = GMap.pixelToTile(entity.getX());

        boolean playerInRange = isPlayerInRange(playerRow, playerCol, onealRow, onealCol);
        directionChangeTimer += tpf;

        if (playerInRange) {
            if (!isChasing) {
                setSpeed(ONEAL_CHASE_SPEED);
                isChasing = true;
            }
        } else if (isChasing) {
            setSpeed(ONEAL_NORMAL_SPEED);
            isChasing = false;
        }

        move(tpf);

        if (!isChasing) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDirectionChangeTime > DIRECTION_CHANGE_INTERVAL) {
                changeDirection();
                lastDirectionChangeTime = currentTime;
            }
        }

        checkAndSnapIfStuck(tpf);
    }

    @Override
    protected void move(double tpf) {
        if (isDead) return;

        if (!isChasing) {
            super.move(tpf);
            return;
        }

        double playerX = player.getEntity().getX();
        double playerY = player.getEntity().getY();
        double dx = playerX - entity.getX();
        double dy = playerY - entity.getY();

        double nextX = entity.getX();
        double nextY = entity.getY();

        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx > 0) moveRight(); else moveLeft();

            double tempNextX = nextX + (dx > 0 ? speed * tpf : -speed * tpf);
            if (!checkCollisionWithWallOrBrick(tempNextX, nextY)) {
                nextX = tempNextX;
            } else {
                if (dy > 0) moveDown(); else moveUp();
                double tempNextY = nextY + (dy > 0 ? speed * tpf : -speed * tpf);
                if (!checkCollisionWithWallOrBrick(nextX, tempNextY)) {
                    nextY = tempNextY;
                }
            }
        } else {
            if (dy > 0) moveDown(); else moveUp();

            double tempNextY = nextY + (dy > 0 ? speed * tpf : -speed * tpf);
            if (!checkCollisionWithWallOrBrick(nextX, tempNextY)) {
                nextY = tempNextY;
            } else {
                if (dx > 0) moveRight(); else moveLeft();
                double tempNextX = nextX + (dx > 0 ? speed * tpf : -speed * tpf);
                if (!checkCollisionWithWallOrBrick(tempNextX, nextY)) {
                    nextX = tempNextX;
                }
            }
        }

        entity.setPosition(nextX, nextY);
        this.x = (int) nextX;
        this.y = (int) nextY;
    }

    private void checkAndSnapIfStuck(double tpf) {
        double deltaX = Math.abs(entity.getX() - lastX);
        double deltaY = Math.abs(entity.getY() - lastY);

        if (deltaX < MOVE_EPSILON && deltaY < MOVE_EPSILON) {
            stuckTimer += tpf;
            if (stuckTimer > STUCK_THRESHOLD) {
                snapToTileCenter();
                stuckTimer = 0;
            }
        } else {
            stuckTimer = 0;
        }

        lastX = entity.getX();
        lastY = entity.getY();
    }

    private void snapToTileCenter() {
        double tileCenterX = GMap.tileToPixel(GMap.pixelToTile(entity.getX()));
        double tileCenterY = GMap.tileToPixel(GMap.pixelToTile(entity.getY()));
        entity.setPosition(tileCenterX, tileCenterY);
    }

    @Override
    public void handleCollision() {
        if (!isChasing) {
            super.handleCollision();
            return;
        } else {
            isChasing = false;
        }
    }
}
