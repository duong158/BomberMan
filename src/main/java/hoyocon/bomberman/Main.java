package hoyocon.bomberman;

import javafx.scene.Scene;
import javafx.application.Application;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: white;");

        // Tạo nhân vật là một hình chữ nhật (có thể thay bằng hình ảnh)
        Rectangle bomber = new Rectangle(50, 50, 40, 40); // Tạo một hình vuông làm nhân vật
        bomber.setFill(Color.BLUE); // Màu của nhân vật

        // Đặt vị trí ban đầu của nhân vật
        bomber.setX(100);
        bomber.setY(100);

        // Thêm nhân vật vào gamePane
        gamePane.getChildren().add(bomber);

        // Xử lý sự kiện bàn phím để di chuyển nhân vật
        gamePane.setOnKeyPressed(event -> {
            double x = bomber.getX();
            double y = bomber.getY();

            if (event.getCode() == KeyCode.W) {
                bomber.setY(y - 100); // Di chuyển lên
            } else if (event.getCode() == KeyCode.S) {
                bomber.setY(y + 100); // Di chuyển xuống
            } else if (event.getCode() == KeyCode.A) {
                bomber.setX(x - 100); // Di chuyển sang trái
            } else if (event.getCode() == KeyCode.D) {
                bomber.setX(x + 100); // Di chuyển sang phải
            }
        });

        // Tạo Scene và đặt kích thước
        Scene scene = new Scene(gamePane, 800, 600);

        // Đảm bảo Scene nhận sự kiện bàn phím

        // Cấu hình và hiển thị cửa sổ
        primaryStage.setTitle("Bomberman Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        gamePane.requestFocus();
    }
}
