package hoyocon.bomberman.Camera;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CameraStorm {

    private final Pane fogPane;
    private final Group world;
    private final Node target;
    private final int screenWidth;
    private final int screenHeight;
    private final int worldWidth;
    private final int worldHeight;
    private final double lerpFactor = 0.1;

    private final Rectangle fog;
    private final Canvas rainCanvas;
    private final GraphicsContext rainGC;
    private final List<RainDrop> rainDrops;

    private final Rectangle lightningFlash;
    private double lightningTimer = 0;
    private double lightningCooldown = 3 + Math.random() * 5;

    private boolean isShaking = false;
    private double shakeIntensity = 0;
    private double shakeDuration = 0;
    private double currentShakeTime = 0;

    private final Random random = new Random();

    private enum LightningPhase { NONE, FLICKER, FLASH, RECOVER }
    private LightningPhase lightningPhase = LightningPhase.NONE;
    private double lightningPhaseTimer = 0;
    private int flickerCount = 0;
    private int maxFlickers = 0;
    private double nextFlickerTime = 0;


    public CameraStorm(Pane fogPane, Group world, Node target, int screenWidth, int screenHeight, int worldWidth, int worldHeight) {
        this.fogPane = fogPane;
        this.world = world;
        this.target = target;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        // Fog
        fog = new Rectangle(0, 0, screenWidth, screenHeight);
        fog.setMouseTransparent(true);


        // Rain
        rainCanvas = new Canvas(screenWidth, screenHeight);
        rainGC = rainCanvas.getGraphicsContext2D();
        rainCanvas.setMouseTransparent(true);


        rainDrops = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            rainDrops.add(new RainDrop(worldWidth, worldHeight));
        }

        // Lightning overlay
        lightningFlash = new Rectangle(0, 0, screenWidth, screenHeight);
        lightningFlash.setFill(Color.TRANSPARENT);
        lightningFlash.setMouseTransparent(true);
        lightningFlash.setBlendMode(BlendMode.ADD);


        fogPane.getChildren().addAll(fog, lightningFlash, rainCanvas);


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
        if (target == null || !target.isVisible()) return;

        Bounds b = target.getBoundsInParent();
        double centerX = b.getMinX() + b.getWidth() / 2.0;
        double centerY = b.getMinY() + b.getHeight() / 2.0;

        double targetX = calculateTargetX(centerX);
        double targetY = calculateTargetY(centerY);

        if (isShaking) {
            currentShakeTime += 1.0 / 60.0;
            if (currentShakeTime < shakeDuration) {
                targetX += (Math.random() * 2 - 1) * shakeIntensity;
                targetY += (Math.random() * 2 - 1) * shakeIntensity;
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

        // Fog effect
        fog.setFill(new RadialGradient(
                0, 0,
                gradientCenterX, gradientCenterY,
                0.3,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.TRANSPARENT),
                new Stop(0.1, Color.color(0, 0, 0, 0.2)),
                new Stop(0.4, Color.color(0, 0, 0, 0.6)),
                new Stop(1, Color.color(0, 0, 0, 0.95))
        ));

        // Rain
        rainGC.clearRect(0, 0, screenWidth, screenHeight);
        rainGC.setStroke(Color.rgb(150, 150, 255, 0.4));
        rainGC.setLineWidth(2.5);   // nét dày hơn

        for (RainDrop drop : rainDrops) {
            drop.update();
            rainGC.strokeLine(
                    drop.x + world.getTranslateX(),
                    drop.y + world.getTranslateY(),
                    drop.x + world.getTranslateX() + drop.dx,
                    drop.y + world.getTranslateY() + drop.dy
            );
        }


        // Lightning
        // Lightning
        lightningTimer += 1.0 / 60.0;

        if (lightningPhase == LightningPhase.NONE && lightningTimer >= lightningCooldown) {
            lightningTimer = 0;
            lightningCooldown = 3 + Math.random() * 5;

            lightningPhase = LightningPhase.FLICKER;
            lightningPhaseTimer = 0;
            flickerCount = 0;
            maxFlickers = 3 + random.nextInt(4); // 3–6 lần nháy
            nextFlickerTime = 0.02 + random.nextDouble() * 0.08; // delay ban đầu cho nháy đầu tiên
        }

        switch (lightningPhase) {
            case FLICKER -> {
                lightningPhaseTimer += 1.0 / 60.0;

                if (lightningPhaseTimer >= nextFlickerTime) {
                    lightningPhaseTimer = 0;
                    flickerCount++;

                    // Ngẫu nhiên độ sáng (0.3 đến 0.9)
                    double flickerBrightness = 0.3 + random.nextDouble() * 0.6;
                    fog.setOpacity(flickerBrightness);

                    // Ngẫu nhiên thời gian cho nháy tiếp theo
                    nextFlickerTime = 0.05 + random.nextDouble() * 0.2;

                    if (flickerCount >= maxFlickers) {
                        lightningPhase = LightningPhase.FLASH;
                        lightningPhaseTimer = 0;
                        fog.setOpacity(0);  // hiện map
                    }
                }
            }
            case FLASH -> {
                lightningPhaseTimer += 1.0 / 60.0;
                if (lightningPhaseTimer >= 0.12) {
                    lightningPhase = LightningPhase.RECOVER;
                    lightningPhaseTimer = 0;
                }
            }
            case RECOVER -> {
                lightningPhaseTimer += 1.0 / 60.0;
                fog.setOpacity(Math.min(1.0, lightningPhaseTimer * 2));  // làm tối lại nhanh
                if (fog.getOpacity() >= 1.0) {
                    lightningPhase = LightningPhase.NONE;
                }
            }
        }


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

    // ===== Inner class for raindrop ====
    private static class RainDrop {
        double x, y, dx, dy;
        double speed;
        private final int worldWidth;
        private final int worldHeight;

        RainDrop(int worldWidth, int worldHeight) {
            this.worldWidth = worldWidth;
            this.worldHeight = worldHeight;
            reset();
        }

        void reset() {
            x = Math.random() * worldWidth;
            y = Math.random() * worldHeight;
            dx = -1.5;
            dy = 4 + Math.random() * 4;
            speed = 1 + Math.random();
        }

        void update() {
            x += dx * speed;
            y += dy * speed;

            if (y > worldHeight || x < 0) {
                reset();
            }
        }
    }
}
