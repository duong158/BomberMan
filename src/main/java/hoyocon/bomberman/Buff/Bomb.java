package hoyocon.bomberman.Buff;

import hoyocon.bomberman.Object.Player;
import javafx.scene.image.Image;

public class Bomb implements BuffGeneric {
    private final int duration = 0;
    private final Image icon = new Image(getClass().getResourceAsStream("/assets/textures/powerup_bombs.png"));

    @Override
    public void apply(Player player) {
        player.setMaxBombs(player.getMaxBombs() + 1);
        player.getActiveBuffs().put("bomb", System.currentTimeMillis());
    }


    @Override
    public void remove(Player player) {
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

