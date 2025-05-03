package hoyocon.bomberman.Save;

import java.io.Serializable;

public class EnemyState implements Serializable {
    public String type; // Ví dụ "Balloon", "Oneal", ...
    public double x, y; // Tọa độ pixel
}

