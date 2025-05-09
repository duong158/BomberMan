package hoyocon.bomberman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    public static Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            mainStage = stage;
            Font.loadFont(
                    GameSceneBuilder.class
                            .getResource("/fonts/PressStart2P-Regular.ttf")
                            .toExternalForm(),
                    10   // size bất kỳ, chỉ để register
            );
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/FXML/Start-view.fxml")
            ));
            Scene scene = new Scene(root, 1920, 1080);

            stage.setTitle("Bomberman Game");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Lỗi khi load FXML:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}