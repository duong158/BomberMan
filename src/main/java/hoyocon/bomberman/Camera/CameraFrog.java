package hoyocon.bomberman.Camera;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

public class CameraFrog {

    private final Pane fogPane;
    private final Group world;
    private final Node target;
    private final int screenWidth;
    private final int screenHeight;
    private final int worldWidth;
    private final int worldHeight;
    private final double lerpFactor = 0.1;
    private final Rectangle fog;
    private boolean isShaking = false;
    private double shakeIntensity = 0;
    private double shakeDuration = 0;
    private double currentShakeTime = 0;
    private double originalX, originalY;

    public CameraFrog(Pane fogPane, Group world, Node target, int screenWidth, int screenHeight, int worldWidth, int worldHeight) {
        this.fogPane = fogPane;
        this.world = world;
        this.target = target;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        fog = new Rectangle(0, 0, screenWidth, screenHeight);
        fog.setMouseTransparent(true);
        fogPane.getChildren().add(fog);

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

    public void update() {
        Bounds b = target.getBoundsInParent();
        if (target == null || !target.isVisible()) return;

        double centerX = b.getMinX() + b.getWidth() / 2.0;
        double centerY = b.getMinY() + b.getHeight() / 2.0;

        double targetX = calculateTargetX(centerX);
        double targetY = calculateTargetY(centerY);

        if (isShaking) {
            currentShakeTime += 1.0 / 60.0;

            if (currentShakeTime < shakeDuration) {
                // Tính toán độ rung
                double shakeOffsetX = (Math.random() * 2 - 1) * shakeIntensity;
                double shakeOffsetY = (Math.random() * 2 - 1) * shakeIntensity;

                targetX += shakeOffsetX;
                targetY += shakeOffsetY;

                // Giảm dần cường độ
                shakeIntensity *= 0.9;
            } else {
                isShaking = false;
            }
        }

        world.setTranslateX(lerp(world.getTranslateX(), targetX, lerpFactor));
        world.setTranslateY(lerp(world.getTranslateY(), targetY, lerpFactor));

        double screenTargetX = centerX + world.getTranslateX();
        double screenTargetY = centerY + world.getTranslateY();

        double gradientCenterX = screenTargetX / screenWidth;
        double gradientCenterY = screenTargetY / screenHeight;

        RadialGradient fogGradient = new RadialGradient(
                0, 0,
                gradientCenterX, gradientCenterY,
                0.9,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.07, Color.TRANSPARENT),
                new Stop(0.14, Color.color(0, 0, 0, 0.6)),
                new Stop(0.35, Color.color(0, 0, 0, 0.9)),
                new Stop(1, Color.color(0, 0, 0, 1.0))
        );
        fog.setFill(fogGradient);
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

    public void startShake(double intensity, double duration) {
        this.isShaking = true;
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
        this.currentShakeTime = 0;
        this.originalX = world.getTranslateX();
        this.originalY = world.getTranslateY();
    }

    public void reset() {
        Bounds b = target.getBoundsInParent();
        double centerX = b.getMinX() + b.getWidth() / 2.0;
        double centerY = b.getMinY() + b.getHeight() / 2.0;

        double tx = calculateTargetX(centerX);
        double ty = calculateTargetY(centerY);

        isShaking = false;
        shakeIntensity = 0;
        shakeDuration = 0;
        currentShakeTime = 0;

        world.setTranslateX(tx);
        world.setTranslateY(ty);
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}