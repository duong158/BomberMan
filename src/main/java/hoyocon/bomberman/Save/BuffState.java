package hoyocon.bomberman.Save;

import java.io.Serializable;

public class BuffState implements Serializable {
    public String buffType; // Ví dụ "speed", "flameRange"
    public double x, y;     // Tọa độ pixel
}
