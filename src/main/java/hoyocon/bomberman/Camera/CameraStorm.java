package hoyocon.bomberman.Camera;

import javafx.animation.AnimationTimer;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
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
    private final Pane lightningPane;
    private double lightningTimer = 0;
    private double lightningCooldown = 3 + Math.random() * 5;
    private final List<List<Point2D>> bolts = new ArrayList<>();
    private final List<Double> boltLives = new ArrayList<>();

    // strike region
    private final double minStrikeRadius = 150;
    private final double maxStrikeRadius = 500;

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

        fog = new Rectangle(0, 0, screenWidth, screenHeight);
        fog.setMouseTransparent(true);

        rainCanvas = new Canvas(screenWidth, screenHeight);
        rainGC = rainCanvas.getGraphicsContext2D();
        rainCanvas.setMouseTransparent(true);

        rainDrops = new ArrayList<>();
        for (int i = 0; i < 500; i++) rainDrops.add(new RainDrop(worldWidth, worldHeight));

        lightningFlash = new Rectangle(0, 0, screenWidth, screenHeight);
        lightningFlash.setFill(Color.TRANSPARENT);
        lightningFlash.setMouseTransparent(true);
        lightningFlash.setBlendMode(BlendMode.ADD);

        lightningPane = new Pane();
        lightningPane.setPickOnBounds(false);

        fogPane.getChildren().addAll(fog, lightningFlash, rainCanvas, lightningPane);
        start();
    }

    private void start() {
        new AnimationTimer() {
            @Override public void handle(long now) { update(); }
        }.start();
    }

    public void update() {
        if (target == null || !target.isVisible()) return;

        Bounds b = target.getBoundsInParent();
        double cx = b.getMinX() + b.getWidth() / 2.0;
        double cy = b.getMinY() + b.getHeight() / 2.0;

        double targetX = calculateTargetX(cx);
        double targetY = calculateTargetY(cy);

        if (isShaking) {
            currentShakeTime += 1.0 / 60.0;
            if (currentShakeTime < shakeDuration) {
                targetX += (random.nextDouble() * 2 - 1) * shakeIntensity;
                targetY += (random.nextDouble() * 2 - 1) * shakeIntensity;
                shakeIntensity *= 0.9;
            } else isShaking = false;
        }

        world.setTranslateX(lerp(world.getTranslateX(), targetX, lerpFactor));
        world.setTranslateY(lerp(world.getTranslateY(), targetY, lerpFactor));

        double screenX = cx + world.getTranslateX();
        double screenY = cy + world.getTranslateY();

        fog.setFill(new RadialGradient(0, 0, screenX/screenWidth, screenY/screenHeight, 0.3, true,
                CycleMethod.NO_CYCLE, new Stop(0, Color.TRANSPARENT), new Stop(0.1, Color.color(0,0,0,0.2)),
                new Stop(0.4, Color.color(0,0,0,0.6)), new Stop(1, Color.color(0,0,0,0.95))));

        rainGC.clearRect(0,0,screenWidth,screenHeight);
        rainGC.setStroke(Color.rgb(150,150,255,0.4)); rainGC.setLineWidth(2.5);
        for (RainDrop d : rainDrops) { d.update(); rainGC.strokeLine(d.x+world.getTranslateX(), d.y+world.getTranslateY(), d.x+world.getTranslateX()+d.dx, d.y+world.getTranslateY()+d.dy); }

        lightningTimer += 1.0/60.0;
        if (lightningPhase == LightningPhase.NONE && lightningTimer >= lightningCooldown) {
            lightningTimer = 0;
            lightningCooldown = 3 + random.nextDouble()*5;
            lightningPhase = LightningPhase.FLICKER; lightningPhaseTimer=0; flickerCount=0;
            maxFlickers = 3 + random.nextInt(4);
            nextFlickerTime = 0.02 + random.nextDouble()*0.08;
        }

        switch (lightningPhase) {
            case FLICKER -> handleFlicker();
            case FLASH   -> handleFlash(screenX, screenY);
            case RECOVER -> handleRecover();
        }

        // draw bolts thicker
        for (int i = bolts.size()-1; i >= 0; i--) {
            List<Point2D> bolt = bolts.get(i);
            double life = boltLives.get(i);
            if (life > 0) {
                rainGC.setStroke(Color.rgb(255,255,200,0.9));
                rainGC.setLineWidth(4);  // tăng độ dày
                Point2D p0 = bolt.get(0);
                for (int j=1; j<bolt.size(); j++) {
                    Point2D p1 = bolt.get(j);
                    rainGC.strokeLine(p0.getX(), p0.getY(), p1.getX(), p1.getY()); p0 = p1;
                }
                boltLives.set(i, life - 1.0/60.0);
            } else {
                bolts.remove(i); boltLives.remove(i);
            }
        }
    }

    private void handleFlicker() {
        lightningPhaseTimer += 1.0/60.0;
        if (lightningPhaseTimer >= nextFlickerTime) {
            lightningPhaseTimer=0; flickerCount++;
            double bright=0.3+random.nextDouble()*0.6; fog.setOpacity(bright);
            startShake(bright*12, 0.1+random.nextDouble()*0.1);
            nextFlickerTime = 0.05+random.nextDouble()*0.2;
            if (flickerCount>=maxFlickers) { lightningPhase=LightningPhase.FLASH; lightningPhaseTimer=0; fog.setOpacity(0); }
        }
    }

    private void handleFlash(double tx, double ty) {
        if (lightningPhaseTimer == 0) {
            startShake(500,0.3);
            int count = 5 + random.nextInt(4); // 5–8 bolts
            for (int i=0; i<count; i++) {
                double angle = random.nextDouble() * 2 * Math.PI;
                double radius = minStrikeRadius + random.nextDouble() * (maxStrikeRadius - minStrikeRadius);
                double endX = tx + Math.cos(angle) * radius;
                double endY = ty + Math.sin(angle) * radius;
                // chọn cạnh ngẫu nhiên làm start
                int side = random.nextInt(4);
                double startX, startY;
                switch (side) {
                    case 0 -> { startX = random.nextDouble() * screenWidth; startY = 0; }
                    case 1 -> { startX = random.nextDouble() * screenWidth; startY = screenHeight; }
                    case 2 -> { startX = 0; startY = random.nextDouble() * screenHeight; }
                    default -> { startX = screenWidth; startY = random.nextDouble() * screenHeight; }
                }
                bolts.add(generateFractalBolt(startX, startY, endX, endY, 6, screenHeight/3.0));
                boltLives.add(0.2);
            }
        }
        lightningPhaseTimer += 1.0/60.0;
        if (lightningPhaseTimer < 0.12) lightningFlash.setFill(Color.color(1,1,1,0.8));
        else { lightningPhase=LightningPhase.RECOVER; lightningPhaseTimer=0; lightningFlash.setFill(Color.TRANSPARENT); }
    }

    private void handleRecover() {
        lightningPhaseTimer += 1.0/60.0;
        fog.setOpacity(Math.min(1.0, lightningPhaseTimer*2));
        if (fog.getOpacity()>=1.0) lightningPhase=LightningPhase.NONE;
    }

    private List<Point2D> generateFractalBolt(double x1,double y1,double x2,double y2,int depth,double disp) {
        List<Point2D> pts=new ArrayList<>();
        if(depth==0){ pts.add(new Point2D(x1,y1)); pts.add(new Point2D(x2,y2)); }
        else{
            double mx=(x1+x2)/2, my=(y1+y2)/2;
            double dx=x2-x1, dy=y2-y1, len=Math.hypot(dx,dy);
            double ux=-dy/len, uy=dx/len;
            double off=(random.nextDouble()*2-1)*disp;
            mx+=ux*off; my+=uy*off;
            List<Point2D> a=generateFractalBolt(x1,y1,mx,my,depth-1,disp/2);
            List<Point2D> b=generateFractalBolt(mx,my,x2,y2,depth-1,disp/2);
            pts.addAll(a); pts.addAll(b.subList(1,b.size()));
        }
        return pts;
    }

    private double calculateTargetX(double c) { double t=screenWidth/2-c; return worldWidth>screenWidth?Math.max(Math.min(0,t),screenWidth-worldWidth):(screenWidth-worldWidth)/2; }
    private double calculateTargetY(double c) { double t=screenHeight/2-c; return worldHeight>screenHeight?Math.max(Math.min(0,t),screenHeight-worldHeight):(screenHeight-worldHeight)/2; }
    public void startShake(double i,double d){isShaking=true;shakeIntensity=i;shakeDuration=d;currentShakeTime=0;}
    public void reset(){Bounds b=target.getBoundsInParent();world.setTranslateX(calculateTargetX(b.getMinX()+b.getWidth()/2));world.setTranslateY(calculateTargetY(b.getMinY()+b.getHeight()/2));isShaking=false;}
    private double lerp(double a,double b,double t){return a+(b-a)*t;}
    private static class RainDrop{double x,y,dx,dy,s;int w,h;RainDrop(int w,int h){this.w=w;this.h=h;reset();}void reset(){x=Math.random()*w;y=Math.random()*h;dx=-1.5;dy=4+Math.random()*4;s=1+Math.random();}void update(){x+=dx*s;y+=dy*s;if(y>h||x<0)reset();}}
}
