package hoyocon.bomberman.Map;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.BoundingShape;
import hoyocon.bomberman.Buff.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;


import java.util.*;

public class GMap {
    private BuffGeneric[][] hiddenBuffs;
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
    public static final int ENTRANCE = 3;
    public static final int EXIT = 4;
    public static final int BALLOON = 5;
    public static final int PASS = 6;
    public static final int ONEAL = 7;
    public static final int DAHL = 8;
    public static final int DORIA = 9;

    // Ảnh cho các tile
    private Image wallImage;
    private Image brickImage;
    private Image emptyImage;
    private Image exitImage;
    private Image entranceImage;

    // Hitbox data for walls and bricks
    private boolean[][] wallHitbox;
    private boolean[][] brickHitbox;


    public GMap(int[][] mapData) {
        this.mapData = mapData;
        this.height = mapData.length;
        this.width = mapData[0].length;

        canvas = new Canvas(width * TILE_SIZE, height * TILE_SIZE);
        gc = canvas.getGraphicsContext2D();

        wallHitbox = new boolean[height][width];
        brickHitbox = new boolean[height][width];

        initializeHitboxes();
        loadImages();
        // Khởi tạo mảng và gán 15 buff ngẫu nhiên vào các vị trí brick
        hiddenBuffs = new BuffGeneric[height][width];
        assignHiddenBuffs(100);
    }

    private void assignHiddenBuffs(int count) {
        List<int[]> bricks = new ArrayList<>();
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (brickHitbox[r][c])  // nếu là brick
                    bricks.add(new int[]{r, c});
            }
        }
        Collections.shuffle(bricks);
        BuffGeneric[] types = { new Bomb(), new Speed(), new Flame(), new Heal(), new FlamePass()};
        Random rnd = new Random();
        for (int i = 0; i < count && i < bricks.size(); i++) {
            int[] p = bricks.get(i);
            hiddenBuffs[p[0]][p[1]] = types[rnd.nextInt(types.length)];
        }
    }

    public BuffGeneric getHiddenBuff(int row, int col) {
        return hiddenBuffs[row][col];
    }
    public void clearHiddenBuff(int row, int col) {
        hiddenBuffs[row][col] = null;
    }
    // Phương thức xoá brick (khi bị nổ)
    public void removeBrick(int row, int col) {
        mapData[row][col] = EMPTY;
        brickHitbox[row][col] = false;
        // Vẽ lại ô trống lên canvas
        gc.drawImage(emptyImage, col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private void initializeHitboxes() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (mapData[row][col] == WALL) {
                    wallHitbox[row][col] = true;
                } else if (mapData[row][col] == BRICK) {
                    brickHitbox[row][col] = true;
                } else {
                    wallHitbox[row][col] = false;
                    brickHitbox[row][col] = false;
                }
            }
        }
    }

    // Method to get balloon spawn positions
    public List<int[]> getESpawnPositions(int enemyType) {
        List<int[]> positions = new ArrayList<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (mapData[row][col] == enemyType) {
                    positions.add(new int[]{row, col});
                    mapData[row][col] = EMPTY;
                }
            }
        }
        return positions;
    }

    private void loadImages() {
        // Tải các hình ảnh cho map
        wallImage = new Image(getClass().getResourceAsStream("/assets/textures/wall.png"));
        brickImage = new Image(getClass().getResourceAsStream("/assets/textures/brick.png"));
        emptyImage = new Image(getClass().getResourceAsStream("/assets/textures/empty.png"));
        exitImage = new Image(getClass().getResourceAsStream("/assets/textures/portal.png"));
        entranceImage = new Image(getClass().getResourceAsStream("/assets/textures/gate1.png"));
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
                    case EXIT:
                        gc.drawImage(exitImage, x, y, TILE_SIZE, TILE_SIZE);
                        break;
                    case ENTRANCE:
                        gc.drawImage(entranceImage, x, y, TILE_SIZE, TILE_SIZE);
                        break;
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
            return -1; // Giá trị không hợp lệ
        }
        return mapData[row][col];
    }

    public boolean isWalkable(int row, int col) {
        int tileType = getTileType(row, col);
        return tileType != WALL && tileType != BRICK;
    }

    public static int pixelToTile(double pixel) {
        return (int) (pixel / TILE_SIZE);
    }
    public static double tileToPixel(int tile){
        return (double) tile*TILE_SIZE;
    }

    public static boolean isPlayerInTile(double playerX, double playerY, double playerWidth, double playerHeight) {
        // Chuyển đổi tọa độ pixel của hai góc hitbox sang tọa độ tile
        int topLeftRow = pixelToTile(playerY);
        int topLeftCol = pixelToTile(playerX);
        int bottomRightRow = pixelToTile(playerY + playerHeight - 1);
        int bottomRightCol = pixelToTile(playerX + playerWidth - 1);

        // Kiểm tra xem hai góc có nằm trong cùng một ô không
        return topLeftRow == bottomRightRow && topLeftCol == bottomRightCol;
    }

//    public boolean canMoveTo(double x, double y, double width, double height) {
//        int topLeftRow = pixelToTile(y);
//        int topLeftCol = pixelToTile(x);
//        int topRightRow = pixelToTile(y);
//        int topRightCol = pixelToTile(x + width - 1);
//        int bottomLeftRow = pixelToTile(y + height - 1);
//        int bottomLeftCol = pixelToTile(x);
//        int bottomRightRow = pixelToTile(y + height - 1);
//        int bottomRightCol = pixelToTile(x + width - 1);
//
//        return isWalkable(topLeftRow, topLeftCol) &&
//               isWalkable(topRightRow, topRightCol) &&
//               isWalkable(bottomLeftRow, bottomLeftCol) &&
//               isWalkable(bottomRightRow, bottomRightCol);
//    }

    public boolean isWallHitbox(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width && wallHitbox[row][col];
    }

    public boolean isBrickHitbox(int row, int col) {
        return row >= 0 && row < height && col >= 0 && col < width && brickHitbox[row][col];
    }

    public boolean checkCollisionWithWall(double x, double y, double width, double height) {
        int topLeftRow = pixelToTile(y);
        int topLeftCol = pixelToTile(x);
        int topRightRow = pixelToTile(y);
        int topRightCol = pixelToTile(x + width - 1);
        int bottomLeftRow = pixelToTile(y + height - 1);
        int bottomLeftCol = pixelToTile(x);
        int bottomRightRow = pixelToTile(y + height - 1);
        int bottomRightCol = pixelToTile(x + width - 1);

        return isWallHitbox(topLeftRow, topLeftCol) ||
               isWallHitbox(topRightRow, topRightCol) ||
               isWallHitbox(bottomLeftRow, bottomLeftCol) ||
               isWallHitbox(bottomRightRow, bottomRightCol);
    }

    public boolean checkCollisionWithBrick(double x, double y, double width, double height) {
        int topLeftRow = pixelToTile(y);
        int topLeftCol = pixelToTile(x);
        int topRightRow = pixelToTile(y);
        int topRightCol = pixelToTile(x + width - 1);
        int bottomLeftRow = pixelToTile(y + height - 1);
        int bottomLeftCol = pixelToTile(x);
        int bottomRightRow = pixelToTile(y + height - 1);
        int bottomRightCol = pixelToTile(x + width - 1);

        return isBrickHitbox(topLeftRow, topLeftCol) ||
               isBrickHitbox(topRightRow, topRightCol) ||
               isBrickHitbox(bottomLeftRow, bottomLeftCol) ||
               isBrickHitbox(bottomRightRow, bottomRightCol);
    }

    public Entity createWallEntity(int row, int col) {
        double x = col * TILE_SIZE;
        double y = row * TILE_SIZE;

        Entity wallEntity = new Entity();
        PhysicsComponent physics = new PhysicsComponent();
        CollidableComponent collidable = new CollidableComponent(true);

        wallEntity.addComponent(physics);
        wallEntity.addComponent(collidable);
        wallEntity.getBoundingBoxComponent().addHitBox(new HitBox("WALL", BoundingShape.box(TILE_SIZE, TILE_SIZE)));
        wallEntity.setPosition(x, y);

        return wallEntity;
    }

    public Entity createBrickEntity(int row, int col) {
        double x = col * TILE_SIZE;
        double y = row * TILE_SIZE;

        Entity brickEntity = new Entity();
        PhysicsComponent physics = new PhysicsComponent();
        CollidableComponent collidable = new CollidableComponent(true);

        brickEntity.addComponent(physics);
        brickEntity.addComponent(collidable);
        brickEntity.getBoundingBoxComponent().addHitBox(new HitBox("BRICK", BoundingShape.box(TILE_SIZE, TILE_SIZE)));
        brickEntity.setPosition(x, y);

        return brickEntity;
    }

    public void initializeEntities() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (mapData[row][col] == WALL) {
                    FXGL.getGameWorld().addEntity(createWallEntity(row, col));
                } else if (mapData[row][col] == BRICK) {
                    FXGL.getGameWorld().addEntity(createBrickEntity(row, col));
                }
            }
        }
    }
    public int[][] getMapDataArray() {
        return this.mapData;
    }
}