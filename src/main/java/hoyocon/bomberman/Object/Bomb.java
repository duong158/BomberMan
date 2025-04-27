package hoyocon.bomberman.Object;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import hoyocon.bomberman.EntitiesState.EntityType;
import javafx.scene.image.Image;
import javafx.util.Duration;

public class Bomb extends Component {
    private Player owner;
    private AnimatedTexture texture;
    private AnimationChannel bombAnimation;

    public Bomb(Player owner) {
        this.owner = owner;

        Image bombImage = new Image(getClass().getResourceAsStream("/assets/textures/bomb.png"));
        bombAnimation = new AnimationChannel(bombImage, 3, 48, 48, Duration.seconds(1), 0, 2);
        texture = new AnimatedTexture(bombAnimation);
    }

    public AnimatedTexture getTexture() {
        return texture;
    }

    @Override
    public void onAdded() {
        System.out.println("Bomb added to world");
        entity.getViewComponent().addChild(texture);
        texture.loop();

        // Schedule explosion
        FXGL.runOnce(this::explode, Duration.seconds(3));
    }

    public Entity createEntity(double x, double y, Player owner) {
        Bomb bombComponent = new Bomb(owner);
        Entity bombEntity = new Entity();
        bombEntity.setType(EntityType.BOMB); // Đặt loại entity
        bombEntity.addComponent(bombComponent);
        bombEntity.setPosition(x, y);
        return bombEntity;
    }

    public void explode() {
        // Logic xử lý khi bom nổ (có thể thêm Animation nổ ở đây)
        entity.removeFromWorld(); // Xóa bom khỏi thế giới
        if (owner != null) {
            owner.bombExploded();
        }
    }
}