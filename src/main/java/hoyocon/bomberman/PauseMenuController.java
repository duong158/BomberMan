package hoyocon.bomberman;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class PauseMenuController {
    @FXML
    private Pane rootPane;
    @FXML
    private Button continueBtn;
    @FXML
    private Button exitBtn;

    private Pane uiPane;

    /** Gọi từ GameSceneBuilder sau khi load FXML để inject uiPane */
    public void setUiPane(Pane uiPane) {
        this.uiPane = uiPane;
    }

    /** Handler cho nút Continue */
    @FXML
    private void onContinue(ActionEvent event) {
        GameSceneBuilder.hidePauseMenu(uiPane);
        if (GameSceneBuilder.gameLoop != null) {
            GameSceneBuilder.gameLoop.start();
        }
        uiPane.getScene().getRoot().requestFocus();
    }
    @FXML
    private void onExit(ActionEvent event) {
        if (GameSceneBuilder.gameLoop != null) GameSceneBuilder.gameLoop.stop();
        Platform.exit();
    }
    @FXML
    private void onMenu(ActionEvent event) {
        if (GameSceneBuilder.gameLoop != null) {
            GameSceneBuilder.gameLoop.stop();
        }
        GameSceneBuilder.hidePauseMenu(uiPane);
        try {
            // Load the main Start view
            Parent startView = FXMLLoader.load(
                    getClass().getResource("/hoyocon/bomberman/Start-view.fxml")
            );
            Scene startScene = new Scene(startView, 1920, 1080);

            Stage stage = (Stage) uiPane.getScene().getWindow();
            stage.setScene(startScene);
            stage.setTitle("Bomberman Menu");
            startScene.getRoot().requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}