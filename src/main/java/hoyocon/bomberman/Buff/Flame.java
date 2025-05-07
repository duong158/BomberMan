package hoyocon.bomberman.Buff;

import hoyocon.bomberman.Object.Player;
import javafx.scene.image.Image;

public class Flame implements BuffGeneric {
    private final int duration = 10;
    private final Image icon = new Image(getClass().getResourceAsStream("/assets/textures/powerup_flames.png"));

    public void apply(Player player) {
        player.increaseFlameRange(1);
    }

    @Override
    public void remove(Player player) {
        player.increaseFlameRange(-1);
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public String getName() {
        return "Flame";
    }

    @Override
    public Image getIcon() {
        return icon;
    }
}