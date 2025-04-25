package hoyocon.bomberman.Object;

import hoyocon.bomberman.Buff.BuffGeneric;
import javafx.scene.image.ImageView;

public class BuffEntity {
    private BuffGeneric buff;
    private ImageView imageView;

    public BuffEntity(BuffGeneric buff, double x, double y) {
        this.buff = buff;
        this.imageView = new ImageView(buff.getIcon());
        this.imageView.setX(x);
        this.imageView.setY(y);
        this.imageView.setFitWidth(40); // Kích thước mặc định
        this.imageView.setFitHeight(40);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public BuffGeneric getBuff() {
        return buff;
    }
}