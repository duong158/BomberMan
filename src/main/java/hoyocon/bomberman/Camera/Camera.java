package hoyocon.bomberman.Camera;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;

public class Camera {

    private final Group world;
    private final Node target;

    private final int screenWidth;
    private final int screenHeight;
    private final int worldWidth;
    private final int worldHeight;

    private final double lerpFactor = 0.1; // độ mượt camera

    public Camera(Group world, Node target, int screenWidth, int screenHeight, int worldWidth, int worldHeight) {
        this.world = world;
        this.target = target;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        start();
    }

    private void start() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
        timer.start();
    }

    private void update() {
        Bounds b = target.getBoundsInParent();  // gồm cả layoutX/Y và translateX/Y
        double centerX = b.getMinX() + b.getWidth()  / 2.0;
        double centerY = b.getMinY() + b.getHeight() / 2.0;

        double targetX = screenWidth / 2 - centerX;
        double targetY = screenHeight / 2 - centerY;

        // Giới hạn không vượt ra ngoài biên
        targetX = Math.min(0, Math.max(screenWidth  - worldWidth,  targetX));
        targetY = Math.min(0, Math.max(screenHeight - worldHeight, targetY));

        world.setTranslateX(lerp(world.getTranslateX(), targetX, lerpFactor));
        world.setTranslateY(lerp(world.getTranslateY(), targetY, lerpFactor));
    }


    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
