package hoyocon.bomberman;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class GameOverController {

    @FXML
    private void onExitClicked(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void onRetryClicked(ActionEvent event) {
        GameSceneBuilder.resetMusic();
        // Tạo và hiển thị màn hình game
        Scene gameScene = GameSceneBuilder.buildNewGameScene();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(gameScene);
        stage.setTitle("Bomberman Game");
        gameScene.getRoot().requestFocus(); // Đảm bảo focus cho game
    }

    @FXML
    private void onMenuClicked(ActionEvent event) {
        GameSceneBuilder.resetMusic();
        try {
            // Load
            Parent menuView = FXMLLoader.load(getClass().getResource("/FXML/Start-view.fxml"));
            Scene menuScene = new Scene(menuView, 1920, 1080);

            // Lấy Stage hiện tại từ sự kiện
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Chuyển Scene sang Menu
            stage.setScene(menuScene);
            stage.setTitle("Bomberman Menu");
            menuScene.getRoot().requestFocus(); // Đảm bảo focus cho Menu
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
