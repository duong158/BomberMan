package hoyocon.bomberman.Map;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.BoundingShape;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GMap {
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
}