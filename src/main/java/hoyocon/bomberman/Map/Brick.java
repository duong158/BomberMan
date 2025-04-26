package hoyocon.bomberman.Map;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;

public class Brick {
    private double x, y;
    private double width, height;
    private Image brickImage;
    private Bounds hitbox;

    public Brick(double x, double y) {
        this.x = x;
        this.y = y;
        this.width = GMap.TILE_SIZE;
        this.height = GMap.TILE_SIZE;
        this.hitbox = new BoundingBox(x, y, width, height);
        this.brickImage = new Image(getClass().getResourceAsStream("/assets/textures/brick.png"));
    }

    public Bounds getHitbox() {
        return hitbox;
    }

    public Image getImage() {
        return brickImage;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
