package hoyocon.bomberman.Object;

import hoyocon.bomberman.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartController {

    @FXML
    private void onMenuClicked(ActionEvent event) throws IOException {
        Parent menuView = FXMLLoader.load(getClass().getResource("/hoyocon/bomberman/Menu-view.fxml"));
        Scene scene = new Scene(menuView, 1920, 1080);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void onStartClicked(ActionEvent event) {
        Scene gameScene = GameSceneBuilder.buildGameScene();
        Main.mainStage.setScene(gameScene);
        Main.mainStage.setTitle("Bomberman Game");

        // GỌI focus sau khi gán scene cho Stage
        gameScene.getRoot().requestFocus();
    }


}
