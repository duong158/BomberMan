package hoyocon.bomberman.Object;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.EntitiesState.EntityType;
import javafx.animation.AnimationTimer;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class Bomb extends Component {
    private Player owner;
    private AnimatedTexture texture;
    private AnimationChannel bombAnimation;

    public Bomb(Player owner) {
        this.owner = owner;

        Image bombImage = new Image(getClass().getResourceAsStream("/assets/textures/bomb.png"));
        bombAnimation = new AnimationChannel(bombImage, 3, 48, 48, Duration.seconds(2), 0, 2);
        texture = new AnimatedTexture(bombAnimation);
    }

    public AnimatedTexture getTexture() {
        return texture;
    }

    @Override
    public void onAdded() {
        entity.getViewComponent().addChild(texture);
        texture.loop();
        FXGL.runOnce(this::explode, Duration.seconds(2)); // hẹn giờ nổ sau 2s
    }

    public Entity createEntity(double x, double y, Player owner) {
        Bomb bombComponent = new Bomb(owner);
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

    public void explode() {
        // Phát âm thanh nổ bom
        FXGL.play("place_bomb.wav");

        double tileSize = 48;
        createExplosion(entity.getX(), entity.getY(), "/assets/textures/central_flame.png"); // Vụ nổ ở giữa
        createExplosion(entity.getX(), entity.getY() - tileSize, "/assets/textures/top_up_flame.png"); // Vụ nổ phía trên
        createExplosion(entity.getX(), entity.getY() + tileSize, "/assets/textures/top_down_flame.png"); // Vụ nổ phía dưới
        createExplosion(entity.getX() - tileSize, entity.getY(), "/assets/textures/top_left_flame.png"); // Vụ nổ bên trái
        createExplosion(entity.getX() + tileSize, entity.getY(), "/assets/textures/top_right_flame.png"); // Vụ nổ bên phải

        entity.removeFromWorld(); // Xóa bom khỏi thế giới
        if (owner != null) {
            owner.bombExploded();
        }
    }

    private void createExplosion(double x, double y, String texturePath) {
        Image explosionImage = new Image(getClass().getResourceAsStream(texturePath));
        AnimationChannel explosionAnimation = new AnimationChannel(explosionImage,
                3, 48, 48, Duration.seconds(1), 0, 2);
        AnimatedTexture explosionTexture = new AnimatedTexture(explosionAnimation);

        Entity explosionEntity = new Entity();
        explosionEntity.setType(EntityType.EXPLOSION); // Đặt loại entity cho vụ nổ
        explosionEntity.setPosition(x, y);
        explosionEntity.getViewComponent().addChild(explosionTexture);
        FXGL.getGameWorld().addEntity(explosionEntity);

        // Thêm hiệu ứng phát sáng nhẹ
        explosionTexture.setEffect(new Glow(0.5));

        explosionTexture.play();

        // Tạo loop cập nhật animation
        AnimationTimer animLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                explosionTexture.onUpdate(1.0 / 60.0);
            }
        };
        animLoop.start();

        // Dừng và xóa entity khi kết thúc animation
        FXGL.runOnce(() -> {
            animLoop.stop();
            explosionEntity.removeFromWorld();
        }, Duration.seconds(1));
    }
}
