package hoyocon.bomberman;

import hoyocon.bomberman.Object.Player;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

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
        /*try {
            // 1. Dừng nhạc nền nếu đang phát
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            // 2. Tìm URL của FXML
            URL guideUrl = StartController.class
                    .getClassLoader()
                    .getResource("hoyocon/bomberman/Guide-view.fxml");
            System.out.println("DEBUG: guideUrl = " + guideUrl);
            if (guideUrl == null) {
                throw new FileNotFoundException(
                        "Không tìm thấy hướng dẫn: /hoyocon/bomberman/Guide-view.fxml"
                );
            }

            // 3. Load và chuyển scene
            Parent guideView = FXMLLoader.load(guideUrl);
            Scene scene = new Scene(guideView, 1920, 1080);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            // 4. Bắt mọi Exception (bao gồm NullPointerException nếu resource null)
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể mở hướng dẫn");
            alert.setContentText(e.getClass().getSimpleName() + ": " + e.getMessage());
            alert.show();
        }*/
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Parent menuView = FXMLLoader.load(getClass().getResource("/hoyocon/bomberman/Guide-view.fxml"));
        Scene scene = new Scene(menuView, 1920, 1080);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
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
        cleanup();
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
// Thêm phương thức để clean up MediaPlayer khi không cần thiết