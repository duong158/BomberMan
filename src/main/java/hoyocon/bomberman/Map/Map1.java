package hoyocon.bomberman.Map;

import hoyocon.bomberman.Object.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Map1 {

    public static final int MOBKC = 5;
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int BRICK = 2;
    public static final int ENTRANCE = 3;
    public static final int EXIT = 4;
    public static final int BALLOON = 5;
    public static final int PASS = 6;
    public static final int ONEAL = 7;
    public static final int DAHL = 8;
    public static final int DORIA = 9;

    public static double MOBNUMS = 5;
    
    // Tỷ lệ phần trăm của từng loại quái
    private static final double BALLOON_PERCENT = 0.3;
    private static final double PASS_PERCENT = 0.2;
    private static final double ONEAL_PERCENT = 0.2;
    private static final double DAHL_PERCENT = 0.2;
    private static final double DORIA_PERCENT = 0.1;

    public static int[][] getMapData(int width, int height, float obstacleDensity) {
        int[][] map = new int[height][width];
        Random rand = new Random();

        //Tạo WALL
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || x == width-1 || y == 0 || y == height-1) {
                    map[y][x] = WALL;
                } else if (x % 2 == 0 && y % 2 == 0) {
                    map[y][x] = WALL;
                } else {
                    map[y][x] = EMPTY;
                }
            }
        }

        //Thêm BRICK
        for (int y = 1; y < height-1; y++) {
            for (int x = 1; x < width-1; x++) {
                if (map[y][x] == EMPTY && rand.nextFloat() < obstacleDensity) {
                    map[y][x] = BRICK;
                }
            }
        }
        List<int[]> emptyPositions = new ArrayList<>();
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (map[y][x] == EMPTY) {
                    emptyPositions.add(new int[]{y, x});
                }
            }
        }

        // Trộn danh sách vị trí trống để đảm bảo ngẫu nhiên
        Collections.shuffle(emptyPositions);
        if(MOBNUMS < emptyPositions.size()) MOBNUMS += Player.getLevel();
        int balloonCount = (int) Math.round(MOBNUMS * BALLOON_PERCENT);
        int passCount = (int) Math.round(MOBNUMS * PASS_PERCENT);
        int onealCount = (int) Math.round(MOBNUMS * ONEAL_PERCENT);
        int dahlCount = (int) Math.round(MOBNUMS * DAHL_PERCENT);
        int doriaCount = (int) Math.round(MOBNUMS * DORIA_PERCENT);


        // Mảng chứa thông tin về các loại quái và số lượng
        int[] mobTypes = {BALLOON, PASS, ONEAL, DAHL, DORIA};
        int[] mobCounts = {balloonCount, passCount, onealCount, dahlCount, doriaCount};
        
        int mobsAdded = 0;
        int mobTypeIndex = 0;  // Loại quái hiện tại đang được spawn
        int currentTypeCount = 0;  // Số lượng đã spawn của loại quái hiện tại
        
        // Spawn quái vật trong một vòng lặp duy nhất
        for (int[] pos : emptyPositions) {
            int y = pos[0];
            int x = pos[1];
            if (map[pos[0]][pos[1]] == EMPTY && pos[0] > MOBKC && pos[1] > MOBKC) {
                boolean surroundedByWall =
                        map[y-1][x] == BRICK || map[y-1][x] == WALL &&
                                map[y+1][x] == BRICK || map[y+1][x] == WALL &&
                                map[y][x-1] == BRICK || map[y][x-1] == WALL &&
                                map[y][x+1] == BRICK || map[y][x+1] == WALL;

                if (surroundedByWall) {
                    continue; // bỏ qua vị trí này
                }
                if (currentTypeCount >= mobCounts[mobTypeIndex]) {
                    mobTypeIndex++;
                    currentTypeCount = 0;
                    if (mobTypeIndex >= mobTypes.length) {
                        break;
                    }
                }

                // Spawn quái vật hiện tại
                map[pos[0]][pos[1]] = mobTypes[mobTypeIndex];
                currentTypeCount++;
                mobsAdded++;

                // Nếu đã spawn đủ tổng số quái vật, thoát khỏi vòng lặp
                if (mobsAdded >= MOBNUMS) {
                    break;
                }
            }
        }

        new Map1().placeEntranceAndExit(map);

        return map;
  
    }

    private void placeEntranceAndExit(int[][] map) {
        int height = map.length;
        int width = map[0].length;

        // Đặt Entrance và Exit
        map[1][1] = ENTRANCE;
        map[height-2][width-2] = EXIT;

        // Tạo ô an toàn quanh Entrance và Exit
        createSafeArea(map, 1, 1);
        createSafeArea(map, width-2, height-2);
    }

    private void createSafeArea(int[][] map, int x, int y) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};


        for (int i = 0; i < 4; i++) {
            int nx = x + dx[i];
            int ny = y + dy[i];
            if (nx > 0 && nx < map[0].length-1 && ny > 0 && ny < map.length-1) {
                if (map[ny][nx] != WALL) {
                    map[ny][nx] = EMPTY;
                }
            }
        }
    }

    public void printMapToConsole(int[][] map) {
        for (int[] ints : map) {
            for (int x = 0; x < map[0].length; x++) {
                System.out.print(ints[x] + " ");
            }
            System.out.println();
        }
    }

}
