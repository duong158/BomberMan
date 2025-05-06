package hoyocon.bomberman.Buff;

import hoyocon.bomberman.Object.Player;
import javafx.scene.image.Image;

public class FlamePass implements BuffGeneric {
    private final int duration = 10;  // giây
    private final Image icon = new Image(getClass()
            .getResourceAsStream("/assets/textures/powerup_flamepass.png"));

    @Override
    public void apply(Player player) {
        player.setFlamePassActive(true);
        // Đăng ký thời điểm buff bắt đầu để Player tự động remove sau duration
        player.getActiveBuffs().put("flamePass", System.currentTimeMillis());
    }

    @Override
    public void remove(Player player) {
        player.setFlamePassActive(false);
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public String getName() {
        return "flamePass";
    }

    @Override
    public Image getIcon() {
        return icon;
    }
}
