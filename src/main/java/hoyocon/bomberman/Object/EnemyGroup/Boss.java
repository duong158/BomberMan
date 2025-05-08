package hoyocon.bomberman.Object.EnemyGroup;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import hoyocon.bomberman.Map.GMap;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;

public class Boss {
    private int x, y;
    private Entity entity;
    private BossAnimationComponent animComponent;
    
    public Boss(int col, int row) {
        this.x = (int) (col * GMap.TILE_SIZE);
        this.y = (int) (row * GMap.TILE_SIZE);
        
        // Khởi tạo component quản lý animation
        animComponent = new BossAnimationComponent();
        
        // Thêm các animation GIF 
        animComponent.addAnimation("idle", "/assets/textures/01_d_idle.gif");
        animComponent.addAnimation("attack", "/assets/textures/03_d_cleave.gif");
        animComponent.addAnimation("hit", "/assets/textures/04_d_take_hit.gif");
        animComponent.addAnimation("dead", "/assets/textures/05_d_death.gif");
    }
    
    public Entity createEntity() {
        entity = FXGL.entityBuilder()
                .at(x, y)
                .with(animComponent)
                .build();
        
        // Mặc định hiển thị animation idle
        playIdle();
        
        return entity;
    }
    
    // Các phương thức điều khiển animation
    public void playIdle() {
        animComponent.playAnimation("idle");
    }
    
    public void playAttack() {
        animComponent.playAnimation("attack");
    }
    
    public void playHit() {
        animComponent.playAnimation("hit");
    }
    
    public void playDead() {
        animComponent.playAnimation("dead");
    }
    
    // Component quản lý animation GIF
    private class BossAnimationComponent extends Component {
        private Map<String, String> animations = new HashMap<>();
        private ImageView currentView;
        private String currentAnim = "";
        
        public BossAnimationComponent() {
            currentView = new ImageView();
            currentView.setPreserveRatio(true);
        }
        
        public void addAnimation(String name, String gifPath) {
            animations.put(name, gifPath);
        }
        
        public void playAnimation(String name) {
            if (!animations.containsKey(name) || name.equals(currentAnim)) {
                return;
            }
            
            currentAnim = name;
            Image gifImage = new Image(getClass().getResourceAsStream(animations.get(name)));
            currentView.setImage(gifImage);
        }
        
        @Override
        public void onAdded() {
            entity.getViewComponent().addChild(currentView);
        }
    }
    
    // Các phương thức khác cho Boss
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public boolean isAlive() {
        // Thêm logic kiểm tra boss còn sống không
        return true;
    }
}
