package hoyocon.bomberman.Buff;
import hoyocon.bomberman.Object.Player;

import javafx.scene.image.Image;

public interface BuffGeneric {
    void apply(Player player); // Gọi khi bắt đầu buff
    void remove(Player player); // Gọi khi buff hết thời gian
    int getDuration(); // Đơn vị: seconds
    String getName();
    Image getIcon();
    default String getType() {
        return getName();
    }
}
