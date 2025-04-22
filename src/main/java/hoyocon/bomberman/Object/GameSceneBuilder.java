package hoyocon.bomberman.Object;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.Cursor;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GameSceneBuilder {
    private static double savedX = 195;
    private static double savedY = 70;

    private static final double screenWidth = 1920;
    private static final double screenHeight = 1080;
    private static final double speed = 20;

    public static Scene buildNewGameScene() {
        // Reset về vị trí ban đầu
        savedX = 195;
        savedY = 70;
        return buildGameScene(savedX, savedY);
    }

    public static Scene buildContinueScene() {
        return buildGameScene(savedX, savedY);
    }

    private static Scene buildGameScene(double startX, double startY) {
        Pane gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: lightgray;");

        Rectangle bomber = new Rectangle(40, 40);
        bomber.setFill(Color.BLUE);

        bomber.setX(startX);
        bomber.setY(startY);

        gamePane.getChildren().add(bomber);

        Scene scene = new Scene(gamePane, screenWidth, screenHeight);

        // ✅ Ẩn con trỏ chuột khi vào game
        scene.setCursor(Cursor.NONE);

        // Focus
        gamePane.setOnMouseClicked(e -> gamePane.requestFocus());
        gamePane.requestFocus();

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
                savedX = bomber.getX();
                savedY = bomber.getY();
                try {
                    Parent menuRoot = FXMLLoader.load(GameSceneBuilder.class.getResource("/hoyocon/bomberman/Menu-view.fxml"));
                    Scene menuScene = new Scene(menuRoot, screenWidth, screenHeight);

                    // ✅ Hiện lại chuột khi quay về menu
                    menuScene.setCursor(Cursor.DEFAULT);

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