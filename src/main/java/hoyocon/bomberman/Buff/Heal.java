package hoyocon.bomberman.Buff;

import hoyocon.bomberman.Object.Player;
import javafx.scene.image.Image;

public class Heal implements BuffGeneric {
    private final int duration = 0;
    private final Image icon = new Image(getClass()
            .getResourceAsStream("/assets/textures/powerup_life.png"));

    @Override
    public void apply(Player player) {
        player.setLives(player.getLives() + 1);
    }

    @Override
    public void remove(Player player) {
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public String getName() {
        return "Heal";
    }

    @Override
    public Image getIcon() {
        return icon;
    }
}