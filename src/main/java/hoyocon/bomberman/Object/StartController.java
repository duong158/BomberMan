package hoyocon.bomberman.Object;

import hoyocon.bomberman.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;

public class StartController {
    @FXML
    private Button sfxOnButton;

    @FXML
    private Button sfxOffButton;

    @FXML
    private Button musicOnButton;

    @FXML
    private Button musicOffButton;

    private MediaPlayer mediaPlayer;
    private boolean isMusicOn = true;
    private boolean isSfxOn = true;

    @FXML
    private void initialize() {
        try {
            // Initialize background music
            var musicResource = getClass().getResource("/assets/music/stage_theme.mp3");
            if (musicResource != null) {
                Media sound = new Media(musicResource.toURI().toString());
                mediaPlayer = new MediaPlayer(sound);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setVolume(0.7);
                mediaPlayer.play();
            } else {
                System.err.println("Music file not found!");
                disableMusicButtons();
            }
        } catch (URISyntaxException e) {
            System.err.println("Music loading error: " + e.getMessage());
            disableMusicButtons();
        }

        updateButtonStates();
    }

    private void disableMusicButtons() {
        musicOnButton.setDisable(true);
        musicOffButton.setDisable(true);
        isMusicOn = false;
    }

    private void updateButtonStates() {
        musicOnButton.setVisible(isMusicOn);
        musicOffButton.setVisible(!isMusicOn);
        sfxOnButton.setVisible(isSfxOn);
        sfxOffButton.setVisible(!isSfxOn);
    }

    @FXML
    private void onMenuClicked(ActionEvent event) throws IOException {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Parent menuView = FXMLLoader.load(getClass().getResource("/hoyocon/bomberman/Menu-view.fxml"));
        Scene scene = new Scene(menuView, 1920, 1080);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void onStartClicked(ActionEvent event) {
        if (mediaPlayer != null && !mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING)) {
            mediaPlayer.seek(mediaPlayer.getStartTime());
            mediaPlayer.play();
        }

        Scene gameScene = GameSceneBuilder.buildNewGameScene();
        Main.mainStage.setScene(gameScene);
        Main.mainStage.setTitle("Bomberman Game");
        gameScene.getRoot().requestFocus();
    }

    @FXML
    private void handleMusicOn() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isMusicOn = true;
            updateButtonStates();
        }
    }

    @FXML
    private void handleMusicOff() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isMusicOn = false;
            updateButtonStates();
        }
    }

    @FXML
    private void handleSfxOn() {
        isSfxOn = true;
        updateButtonStates();
        // Add SFX logic here
    }

    @FXML
    private void handleSfxOff() {
        isSfxOn = false;
        updateButtonStates();
        // Add SFX logic here
    }

    public void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}