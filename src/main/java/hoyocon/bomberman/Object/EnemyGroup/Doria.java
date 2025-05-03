package hoyocon.bomberman.Object.EnemyGroup;

import hoyocon.bomberman.Map.GMap;

import java.util.LinkedList;
import java.util.Queue;

public class Doria extends Oneal {
    private static final double DORIA_NORMAL_SPEED    = 80;
    private static final double DORIA_CHASE_SPEED     = 120;
    private static final int    DORIA_DETECTION_RANGE = 5;

    public Doria(int x, int y) {
        super(x, y, DORIA_NORMAL_SPEED, "/assets/textures/enemy5.png");
    }

    @Override
    protected double getNormalSpeed() {
        return DORIA_NORMAL_SPEED;
    }

    @Override
    protected double getChaseSpeed() {
        return DORIA_CHASE_SPEED;
    }

    @Override
    protected int getDetectionRange() {
        return DORIA_DETECTION_RANGE;
    }
    @Override
    protected boolean checkCollisionWithWallOrBrick(double x, double y) {
        if (gameMap == null) {
            return false;
        }

        // Kích thước hitbox của Enemy
        double width = 48;
        double height = 48;

        // Khi đang trong chế độ truy đuổi, CHỈ kiểm tra va chạm với tường
        if (isChasing) { // Sử dụng phương thức thay vì truy cập biến trực tiếp
            return gameMap.checkCollisionWithWall(x, y, width, height);
        }

        // Khi ở chế độ bình thường, kiểm tra va chạm với cả tường và gạch
        return gameMap.checkCollisionWithWall(x, y, width, height) ||
                gameMap.checkCollisionWithBrick(x, y, width, height);
    }

    @Override
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
                
                // Kiểm tra có nằm trong bản đồ không
                if (newRow < 0 || newRow >= gameMap.height || newCol < 0 || newCol >= gameMap.width || visited[newRow][newCol]) {
                    continue;
                }
                
                // Điều kiện đi được khác nhau tùy theo chế độ
                boolean canWalk;
                
                if (isChasing) {
                    // Khi đang đuổi theo: đi được cả ô trống và ô gạch, chỉ không đi được ô tường
                    int tileType = gameMap.getTileType(newRow, newCol);
                    canWalk = (tileType == GMap.EMPTY || tileType == GMap.BRICK);
                } else {
                    // Khi không đuổi theo: chỉ đi được ô trống
                    canWalk = gameMap.getTileType(newRow, newCol) == GMap.EMPTY;
                }
                
                if (canWalk) {
                    queue.add(new int[]{newRow, newCol});
                    visited[newRow][newCol] = true;
                    parent[newRow][newCol][0] = row;
                    parent[newRow][newCol][1] = col;
                }
            }
        }

        // Phần còn lại giữ nguyên
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
}

