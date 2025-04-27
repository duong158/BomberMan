package hoyocon.bomberman.Map;

import java.util.Random;

public class Map1 {

    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int BRICK = 2;
    public static final int ENTRANCE = 3;
    public static final int EXIT = 4;

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
        placeEntranceAndExit(map);

        return map;
  
    }

    private static void placeEntranceAndExit(int[][] map) {
        int height = map.length;
        int width = map[0].length;

        // Đặt Entrance và Exit
        map[height-2][1] = ENTRANCE;
        map[1][width-2] = EXIT;

        // Tạo ô an toàn quanh Entrance và Exit
        createSafeArea(map, 1, height-2);
        createSafeArea(map, width-2, 1);
    }

    private static void createSafeArea(int[][] map, int x, int y) {
        int[] dx = {-1, 0, 1, 0};
        int[] dy = {0, -1, 0, 1};

        map[y][x] = EMPTY;

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
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                System.out.print(map[y][x] + " ");
            }
            System.out.println();
        }
    }

}
