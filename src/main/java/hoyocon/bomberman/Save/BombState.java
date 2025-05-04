package hoyocon.bomberman.Save;

import java.io.Serializable;

public class BombState implements Serializable {
    public double x, y;          // Tọa độ pixel
    public long timeRemaining;   // Cho đến khi nổ
}