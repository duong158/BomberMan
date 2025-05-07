package hoyocon.bomberman;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import javafx.application.Application;
import javafx.application.Platform;
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
        // Bỏ dòng cấu hình preloader
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            mainStage = stage;
            // Load FXML với đường dẫn chính xác
            Font.loadFont(
                    GameSceneBuilder.class
                            .getResource("/fonts/PressStart2P-Regular.ttf")
                            .toExternalForm(),
                    10   // size bất kỳ, chỉ để register
            );
            Parent root = FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/hoyocon/bomberman/Start-view.fxml")
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