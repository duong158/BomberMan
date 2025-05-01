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
    protected static final double ONEAL_NORMAL_SPEED = 80;
    protected static final double ONEAL_CHASE_SPEED = 100;
    protected static final int DETECTION_RANGE = 5;
    private static final double STUCK_THRESHOLD = 0.3;
    private static final double MOVE_EPSILON = 1.0;

    private double directionChangeTimer = 0;
    private double stuckTimer = 0;
    private double lastX, lastY;

    private Player player;
    protected boolean isChasing = false;
    private boolean isAligning = false;
    private double targetAlignX, targetAlignY;

    public Oneal(int row, int col) {
        super((int)(row * GMap.TILE_SIZE), (int)(col * GMap.TILE_SIZE), ONEAL_NORMAL_SPEED, "/assets/textures/enemy2.png");
    }

    public Oneal(int row, int col, double speed, String assetName) {
        super((int)(row * GMap.TILE_SIZE), (int)(col * GMap.TILE_SIZE), speed, assetName);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    private boolean isPlayerInRange(int playerRow, int playerCol, int onealRow, int onealCol) {
        int distance = Math.abs(playerRow - onealRow) + Math.abs(playerCol - onealCol);
        return distance <= getDetectionRange();
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
                setSpeed(getChaseSpeed());
                isChasing = true;
            }
        } else if (isChasing) {
            setSpeed(getNormalSpeed());
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

        checkAndPrepareAlign(tpf);
    }

    @Override
    protected void move(double tpf) {
        if (isDead) return;

        if (isAligning) {
            moveToAlign(tpf);
            return;
        }

        if (!isChasing) {
            super.move(tpf);
            return;
        }

        int onealRow = GMap.pixelToTile(entity.getY());
        int onealCol = GMap.pixelToTile(entity.getX());
        int playerRow = GMap.pixelToTile(player.getEntity().getY());
        int playerCol = GMap.pixelToTile(player.getEntity().getX());

        int[] dir = findNextDirectionBFS(onealRow, onealCol, playerRow, playerCol);
        if (dir == null) {
            setSpeed(getNormalSpeed());
            isChasing = false;
            super.move(tpf);
            return;
        }

        if (dir[0] == -1) moveUp();
        else if (dir[0] == 1) moveDown();
        else if (dir[1] == -1) moveLeft();
        else if (dir[1] == 1) moveRight();

        double nextX = entity.getX() + dir[1] * speed * tpf;
        double nextY = entity.getY() + dir[0] * speed * tpf;

        if (!checkCollisionWithWallOrBrick(nextX, nextY)) {
            entity.setPosition(nextX, nextY);
            this.x = (int) nextX;
            this.y = (int) nextY;
        }
    }

    protected int[] findNextDirectionBFS(int startRow, int startCol, int targetRow, int targetCol) {
        if (startRow == targetRow && startCol == targetCol) {
            return new int[]{0, 0};
        }

        boolean[][] visited = new boolean[gameMap.height][gameMap.width];
        int[][][] parent = new int[gameMap.height][gameMap.width][2];

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;
        parent[startRow][startCol] = new int[]{-1, -1};

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int row = current[0];
            int col = current[1];

            if (row == targetRow && col == targetCol) {
                break;
            }

            for (int i = 0; i < 4; i++) {
                int newRow = row + dr[i];
                int newCol = col + dc[i];
                if (newRow >= 0 && newRow < gameMap.height && newCol >= 0 && newCol < gameMap.width
                        && gameMap.isWalkable(newRow, newCol) && !visited[newRow][newCol]) {
                    queue.add(new int[]{newRow, newCol});
                    visited[newRow][newCol] = true;
                    parent[newRow][newCol][0] = row;
                    parent[newRow][newCol][1] = col;
                }
            }
        }

        if (!visited[targetRow][targetCol]) {
            return null;
        }

        int row = targetRow;
        int col = targetCol;
        while (!(parent[row][col][0] == startRow && parent[row][col][1] == startCol)) {
            int tempRow = parent[row][col][0];
            int tempCol = parent[row][col][1];
            row = tempRow;
            col = tempCol;
        }

        return new int[]{row - startRow, col - startCol};
    }

    private void checkAndPrepareAlign(double tpf) {
        double deltaX = Math.abs(entity.getX() - lastX);
        double deltaY = Math.abs(entity.getY() - lastY);

        if (deltaX < MOVE_EPSILON && deltaY < MOVE_EPSILON) {
            stuckTimer += tpf;
            if (stuckTimer > STUCK_THRESHOLD) {
                prepareAlignToTileCenter();
                stuckTimer = 0;
            }
        } else {
            stuckTimer = 0;
        }

        lastX = entity.getX();
        lastY = entity.getY();
    }

    private void prepareAlignToTileCenter() {
        targetAlignX = GMap.tileToPixel(GMap.pixelToTile(entity.getX()));
        targetAlignY = GMap.tileToPixel(GMap.pixelToTile(entity.getY()));
        isAligning = true;
    }

    private void moveToAlign(double tpf) {
        double currentX = entity.getX();
        double currentY = entity.getY();

        double dx = targetAlignX - currentX;
        double dy = targetAlignY - currentY;

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 1.0) {
            entity.setPosition(targetAlignX, targetAlignY);
            isAligning = false;
            return;
        }

        double moveX = (dx / distance) * getChaseSpeed() * tpf;
        double moveY = (dy / distance) * getChaseSpeed() * tpf;

        entity.setPosition(currentX + moveX, currentY + moveY);
    }

    @Override
    public void handleCollision() {
        if (!isChasing) {
            super.handleCollision();
        } else {
            isChasing = false;
        }
    }
    protected double getNormalSpeed()    { return ONEAL_NORMAL_SPEED; }
    protected double getChaseSpeed()     { return ONEAL_CHASE_SPEED; }
    protected int    getDetectionRange() { return DETECTION_RANGE;    }

}
