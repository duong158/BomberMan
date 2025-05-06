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

    private boolean isShaking = false;
    private double shakeIntensity = 0;
    private double shakeDuration = 0;
    private double currentShakeTime = 0;
    private double originalX, originalY;

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

    public void startShake(double intensity, double duration) {
        this.isShaking = true;
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
        this.currentShakeTime = 0;
        this.originalX = world.getTranslateX();
        this.originalY = world.getTranslateY();
    }

    public void update() {
        Bounds b = target.getBoundsInParent();
        if (target == null || !target.isVisible()) return;

        double centerX = b.getMinX() + b.getWidth() / 2.0;
        double centerY = b.getMinY() + b.getHeight() / 2.0;

        double targetX = calculateTargetX(centerX);
        double targetY = calculateTargetY(centerY);

        if (isShaking) {
            currentShakeTime += 1.0/60.0;

            if (currentShakeTime < shakeDuration) {
                // Tính toán độ rung
                double shakeOffsetX = (Math.random() * 2 - 1) * shakeIntensity;
                double shakeOffsetY = (Math.random() * 2 - 1) * shakeIntensity;

                targetX += shakeOffsetX;
                targetY += shakeOffsetY;


                shakeIntensity *= 0.9;
            } else {

                isShaking = false;
            }
        }

        world.setTranslateX(lerp(world.getTranslateX(), targetX, lerpFactor));
        world.setTranslateY(lerp(world.getTranslateY(), targetY, lerpFactor));
    }

    private double calculateTargetX(double centerX) {
        double targetX = screenWidth / 2 - centerX;

        if (worldWidth > screenWidth) {
            return Math.max(Math.min(0, targetX), screenWidth - worldWidth);
        } else {
            return (screenWidth - worldWidth) / 2;
        }
    }

    private double calculateTargetY(double centerY) {
        double targetY = screenHeight / 2 - centerY;

        if (worldHeight > screenHeight) {
            return Math.max(Math.min(0, targetY), screenHeight - worldHeight);
        } else {
            return (screenHeight - worldHeight) / 2;
        }
    }
    public void reset() {
        update();

        isShaking = false;
        shakeIntensity = 0;
        shakeDuration = 0;
        currentShakeTime = 0;
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
