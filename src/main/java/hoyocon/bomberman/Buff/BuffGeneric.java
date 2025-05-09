package hoyocon.bomberman.Buff;
import hoyocon.bomberman.Object.Player;

import javafx.scene.image.Image;

public interface BuffGeneric {
    void apply(Player player);
    void remove(Player player);
    int getDuration();
    String getName();
    Image getIcon();
    default String getType() {
        return getName();
    }
}
