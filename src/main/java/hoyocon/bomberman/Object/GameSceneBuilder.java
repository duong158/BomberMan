package hoyocon.bomberman.Object;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GameSceneBuilder {
    public static Scene buildGameScene() {
        double screenWidth = 1920;
        double screenHeight = 1080;
        double speed = 20;

        Pane gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: lightgray;");

        // Tạo nhân vật (kích thước 40x40)
        Rectangle bomber = new Rectangle(40, 40);
        bomber.setFill(Color.BLUE);

        // Đặt vị trí nhân vật ban đầu (nằm hẳn trong màn hình)
        bomber.setX(195);
        bomber.setY(70);

        gamePane.getChildren().add(bomber);

        Scene scene = new Scene(gamePane, screenWidth, screenHeight);

        // Đặt focus cho scene để bắt đầu nhận phím
        scene.setOnMouseClicked(e -> gamePane.requestFocus());
        gamePane.setOnMouseClicked(e -> gamePane.requestFocus());

        // Focus lần đầu
        gamePane.requestFocus();

        // Xử lý di chuyển bằng WASD và chặn ra ngoài màn hình
        gamePane.setOnKeyPressed(event -> {
            double x = bomber.getX();
            double y = bomber.getY();

            if (event.getCode() == KeyCode.W && y - speed >= 0) {
                bomber.setY(y - speed);
            } else if (event.getCode() == KeyCode.S && y + speed + bomber.getHeight() <= screenHeight) {
                bomber.setY(y + speed);
            } else if (event.getCode() == KeyCode.A && x - speed >= 0) {
                bomber.setX(x - speed);
            } else if (event.getCode() == KeyCode.D && x + speed + bomber.getWidth() <= screenWidth) {
                bomber.setX(x + speed);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                try {
                    Parent menuRoot = FXMLLoader.load(GameSceneBuilder.class.getResource("/hoyocon/bomberman/Menu-view.fxml"));
                    Scene menuScene = new Scene(menuRoot, screenWidth, screenHeight);
                    Stage stage = (Stage) scene.getWindow();
                    stage.setScene(menuScene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        return scene;
    }
}
