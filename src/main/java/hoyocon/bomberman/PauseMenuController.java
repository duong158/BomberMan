package hoyocon.bomberman;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.IOException;

import static hoyocon.bomberman.GameSceneBuilder.gameLoop;

public class PauseMenuController {
    @FXML
    private Pane rootPane;
    @FXML
    private Button continueBtn;
    @FXML
    private Button exitBtn;
    @FXML
    private Button musicToggleBtn;

    private Pane uiPane;

    /**
     * Gọi từ GameSceneBuilder sau khi load FXML để inject uiPane
     */
    public void setUiPane(Pane uiPane) {
        this.uiPane = uiPane;
    }

    /**
     * Handler cho nút Continue
     */
    @FXML
    private void onContinue(ActionEvent event) {
        GameSceneBuilder.hidePauseMenu(uiPane);
        if (gameLoop != null) {
            gameLoop.start();
        }
        uiPane.getScene().getRoot().requestFocus();
    }

    private void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ConfirmationDialog.fxml"));
            VBox dialogRoot = loader.load();
            // Configure dialog
            ConfirmationDialogController controller = loader.getController();
            controller.setDialogData(title, message, onConfirm);

            // Create and show dialog
            Scene dialogScene = new Scene(dialogRoot);
            dialogScene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            Stage dialogStage = new Stage();
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dialogStage.setScene(dialogScene);

            // Center on parent
            Stage parentStage = (Stage) uiPane.getScene().getWindow();
            dialogStage.setX(parentStage.getX() + (parentStage.getWidth() - dialogRoot.getPrefWidth()) / 2);
            dialogStage.setY(parentStage.getY() + (parentStage.getHeight() - dialogRoot.getPrefHeight()) / 2);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onExit(ActionEvent event) {
        areUsure();
        showConfirmationDialog(
                "Exit Game",
                "Are you sure you want to exit the game?",
                () -> {
                    if (gameLoop != null) gameLoop.stop();
                    SfxManager.stopWalk();
                    Platform.exit();
                }
        );
    }

    @FXML
    private void onMenu(ActionEvent event) {
        areUsure();
        showConfirmationDialog(
                "Return to Main Menu",
                "Are you sure you want to return to the main menu? Your progress will be lost.",
                () -> {
                    if (gameLoop != null) {
                        gameLoop.stop();
                    }
                    GameSceneBuilder.resetMusic();
                    GameSceneBuilder.hidePauseMenu(uiPane);
                    try {
                        // Load the main Start view
                        Parent startView = FXMLLoader.load(
                                getClass().getResource("/FXML/Start-view.fxml")
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
        );
    }

    @FXML
    private void onToggleMusic(ActionEvent event) {
        // Toggle music state
        GameSceneBuilder.toggleMusic();

        // Update button text based on new state
        updateMusicButtonText();
    }

    private void updateMusicButtonText() {
        if (musicToggleBtn != null) {
            if (GameSceneBuilder.isMusicEnabled()) {
                musicToggleBtn.setText("♫ON");
            } else {
                musicToggleBtn.setText("♫OFF");
            }
        }
    }

    private void areUsure() {
        try{
            Media media = new Media(getClass().getResource("/assets/sounds/omni-man-are-you-sure.mp3").toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            player.play();
        } catch (Exception e) {
            System.out.println("Nah, I'm sure");
        }
    }
}