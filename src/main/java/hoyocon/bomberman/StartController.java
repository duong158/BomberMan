package hoyocon.bomberman;

import hoyocon.bomberman.Object.Player;
import javafx.application.Platform;
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
    private javafx.scene.control.Button autoPlayButton;

    @FXML
    private void onAutoPlayClicked(ActionEvent event) {
        // Đảo trạng thái auto‑play
        GameSceneBuilder.toggleAutoPlay();

        // Cập nhật nhãn theo trạng thái mới
        boolean enabled = getAutoPlayState();
        autoPlayButton.setText(enabled ? "AUTO PLAY: On" : "AUTO PLAY: Off");
    }

    private boolean getAutoPlayState() {
        try {
            java.lang.reflect.Field f = GameSceneBuilder.class.getDeclaredField("autoPlayEnabled");
            f.setAccessible(true);
            return f.getBoolean(null);
        } catch (Exception e) {
            return false;
        }
    }

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
        // Khởi tạo MediaPlayer với nhạc nền từ đường dẫn mới
        try {
            // Sử dụng đường dẫn tương đối trong thư mục resources
            Media sound = new Media(getClass().getResource("/assets/music/main_theme.mp3").toURI().toString());
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Lặp lại vô hạn
            mediaPlayer.setVolume(0.7); // Đặt volume mặc định 70%
            mediaPlayer.play();

            // Thiết lập trạng thái ban đầu của các nút
            musicOffButton.setVisible(false);
        } catch (URISyntaxException e) {
            System.err.println("Error loading background music: " + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("Music file not found at: /assets/music/background_music.mp3");
        }
    }

    @FXML
    private void onHowToPlayClicked(ActionEvent event) throws IOException {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Parent menuView = FXMLLoader.load(getClass().getResource("/FXML/Guide-view.fxml"));
        Scene scene = new Scene(menuView, 1920, 1080);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void onStartClicked(ActionEvent event) {
        cleanup();
        SfxManager.setSfxEnabled(isSfxOn);
        System.out.println("StartController: onStartClicked -> isSfxOn = " + isSfxOn);
        Scene gameScene = GameSceneBuilder.buildNewGameScene();
        Player.level = 1;
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

    private void updateButtonStates() {
        musicOnButton.setVisible(isMusicOn);
        musicOffButton.setVisible(!isMusicOn);
        sfxOnButton.setVisible(isSfxOn);
        sfxOffButton.setVisible(!isSfxOn);
    }
    @FXML
    private void handleSfxOn() {
        isSfxOn = true;
        SfxManager.setSfxEnabled(true);
        updateButtonStates();
    }

    @FXML
    private void handleSfxOff() {
        isSfxOn = false;
        SfxManager.setSfxEnabled(false);
        updateButtonStates();
    }

    public void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }

    @FXML
    private void onExitClicked(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void onCoopClicked(ActionEvent event) {
        cleanup(); // Dừng nhạc nếu có
        System.out.println("[DEBUG] onCoopClicked called");
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Đã nhấn CO-OP!");
        alert.showAndWait();
        try {
            java.net.URL url = getClass().getResource("/FXML/CoopLobby.fxml");
            System.out.println("[DEBUG] getResource /FXML/CoopLobby.fxml: " + url);
            if (url == null) {
                throw new IOException("Không tìm thấy FXML/CoopLobby.fxml trong resource!");
            }
            Parent lobbyView = FXMLLoader.load(url);
            Scene coopScene = new Scene(lobbyView, 1920, 1080);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(coopScene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("Lỗi khi load CoopLobby.fxml: ").append(e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                sb.append("\nCaused by: ").append(cause.getClass().getName()).append(": ").append(cause.getMessage());
                cause = cause.getCause();
            }
            javafx.scene.control.Alert errAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, sb.toString());
            errAlert.showAndWait();
        }
    }
}