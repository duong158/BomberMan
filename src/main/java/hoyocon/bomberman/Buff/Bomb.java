package hoyocon.bomberman.Buff;

import hoyocon.bomberman.Object.Player;
import javafx.scene.image.Image;

public class Bomb implements BuffGeneric {
    private final int duration = 0;  // 0 = vĩnh viễn
    private final Image icon = new Image(getClass().getResourceAsStream("/assets/textures/powerup_bombs.png"));

    @Override
    public void apply(Player player) {
        // Tăng số bom tối đa thêm 1
        player.setMaxBombs(player.getMaxBombs() + 1);
        // Không cần đưa vào activeBuffs nếu vĩnh viễn
    }

    @Override
    public void remove(Player player) {
        // Nếu muốn buff hết hạn 10s thì giảm lại 1 ở đây
        player.setMaxBombs(Math.max(1, player.getMaxBombs() - 1));
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public String getName() {
        return "Bomb";
    }

    @Override
    public Image getIcon() {
        return icon;
    }
}

