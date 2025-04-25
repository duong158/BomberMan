package hoyocon.bomberman.Map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Map {
    public static final double TILE_SIZE = 48;
    private int[][] mapData;
    public int height;
    public int width;
    private Canvas canvas;
    private GraphicsContext gc;

    // Loại ô trong map
    public static final int EMPTY = 0;
    public static final int WALL = 1;
    public static final int BRICK = 2;

    // Ảnh cho các tile
    private Image wallImage;
    private Image brickImage;
    private Image emptyImage;

    public Map(int[][] mapData) {
        this.mapData = mapData;
        this.height = mapData.length;
        this.width = mapData[0].length;

        canvas = new Canvas(width * TILE_SIZE, height * TILE_SIZE);
        gc = canvas.getGraphicsContext2D();

        loadImages();
    }

    private void loadImages() {
        // Tải các hình ảnh cho map
        wallImage = new Image(getClass().getResourceAsStream("/assets/textures/wall.png"));
        brickImage = new Image(getClass().getResourceAsStream("/assets/textures/brick.png"));
        emptyImage = new Image(getClass().getResourceAsStream("/assets/textures/empty.png"));
    }

    public void render() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                // Tính vị trí vẽ
                int x = (int) (col * TILE_SIZE);
                int y = (int) (row * TILE_SIZE);

                // Vẽ tile tương ứng
                switch (mapData[row][col]) {
                    case WALL:
                        gc.drawImage(wallImage, x, y, TILE_SIZE, TILE_SIZE);
                        break;
                    case BRICK:
                        gc.drawImage(brickImage, x, y, TILE_SIZE, TILE_SIZE);
                        break;
                    case EMPTY:
                    default:
                        gc.drawImage(emptyImage, x, y, TILE_SIZE, TILE_SIZE);
                        break;
                }
            }
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public int getTileType(int row, int col) {
        if (row < 0 || row >= height || col < 0 || col >= width) {
            return WALL; // Coi như ngoài map là tường
        }
        return mapData[row][col];
    }

    public boolean isWalkable(int row, int col) {
        return getTileType(row, col) == EMPTY;
    }

    public static int pixelToTile(double pixel) {
        return (int) (pixel / TILE_SIZE);
    }

    public boolean canMoveTo(double x, double y, double width, double height) {
        // Kiểm tra 4 góc của player
        int topLeftRow = pixelToTile(y);
        int topLeftCol = pixelToTile(x);
        int topRightRow = pixelToTile(y);
        int topRightCol = pixelToTile(x + width - 1);
        int bottomLeftRow = pixelToTile(y + height - 1);
        int bottomLeftCol = pixelToTile(x);
        int bottomRightRow = pixelToTile(y + height - 1);
        int bottomRightCol = pixelToTile(x + width - 1);
        
        return isWalkable(topLeftRow, topLeftCol) &&
               isWalkable(topRightRow, topRightCol) &&
               isWalkable(bottomLeftRow, bottomLeftCol) &&
               isWalkable(bottomRightRow, bottomRightCol);
    }
}