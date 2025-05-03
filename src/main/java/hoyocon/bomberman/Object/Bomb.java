package hoyocon.bomberman.Object;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.EntitiesState.EntityType;
import hoyocon.bomberman.GameSceneBuilder;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

public class Bomb extends Component {
    private Player owner;
    private final Pane gamePane;

    private AnimatedTexture texture;
    private AnimationChannel bombAnimation;
    private static final double TILE_SIZE = 48;

    public Bomb(Player owner, Pane gamePane) {
        this.owner = owner;
        this.gamePane = gamePane;

        // Tạo animation cho quả bom
        Image bombImage = new Image(getClass().getResourceAsStream("/assets/textures/bomb.png"));
        bombAnimation = new AnimationChannel(bombImage,
                3, (int)TILE_SIZE, (int)TILE_SIZE,
                Duration.seconds(2), 0, 2);
        texture = new AnimatedTexture(bombAnimation);
    }

    public AnimatedTexture getTexture() {
        return texture;
    }

    @Override
    public void onAdded() {
        // Thêm hình ảnh bom và bắt đầu animation
        entity.getViewComponent().addChild(texture);
        texture.loop();

        // Phát sound đặt bom
        AudioClip placeSfx = new AudioClip(getClass().getResource("/assets/sounds/place_bomb.wav").toString());
        placeSfx.play();

        // Hẹn nổ sau 2s
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> explode());
        delay.play();
    }

    public Entity createEntity(double x, double y, Player owner) {
        Bomb bombComponent = new Bomb(owner, gamePane);
        Entity bombEntity = new Entity();
        bombEntity.setType(EntityType.BOMB); // Đặt loại entity
        bombEntity.addComponent(bombComponent);
        bombEntity.setPosition(x, y);
        return bombEntity;
    }

    @Override
    public void onUpdate(double tpf) {
        texture.onUpdate(tpf); // Cập nhật Animation
    }

    /** Gây nổ bom: tạo hiệu ứng flame xung quanh và play sound */
    public void explode() {
        // Phát âm thanh nổ
        AudioClip explodeSfx = new AudioClip(getClass().getResource("/assets/sounds/explosion.wav").toString());
        explodeSfx.play();

        double x = entity.getX();
        double y = entity.getY();

        // Tạo vụ nổ trung tâm và 4 hướng xung quanh
        createExplosion(x, y, "/assets/textures/central_flame.png");
        createExplosion(x, y - TILE_SIZE, "/assets/textures/top_up_flame.png");
        createExplosion(x, y + TILE_SIZE, "/assets/textures/top_down_flame.png");
        createExplosion(x - TILE_SIZE, y, "/assets/textures/top_left_flame.png");
        createExplosion(x + TILE_SIZE, y, "/assets/textures/top_right_flame.png");

        // Xóa bom khỏi world và cập nhật owner
        entity.removeFromWorld();
        owner.bombExploded();
    }

    private void createExplosion(double x, double y, String texturePath) {
        Image img = new Image(getClass().getResourceAsStream(texturePath));
        AnimationChannel channel = new AnimationChannel(img, 3, 48, 48, Duration.seconds(1), 0, 2);
        AnimatedTexture explosionTexture = new AnimatedTexture(channel);
        explosionTexture.loop();

        Pane flamePane = new Pane(explosionTexture);
        flamePane.setPrefSize(48, 48);
        flamePane.setLayoutX(x);
        flamePane.setLayoutY(y);

        gamePane.getChildren().add(flamePane);
        GameSceneBuilder.explosionEntities.add(flamePane); // ← phải là Pane!

        // Hẹn xóa
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(e -> {
            gamePane.getChildren().remove(flamePane);
            GameSceneBuilder.explosionEntities.remove(flamePane);
        });
        delay.play();
    }
}
