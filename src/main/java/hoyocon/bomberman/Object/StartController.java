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
    private Button musicOnButton;
    @FXML
    private Button musicOffButton;

    private MediaPlayer mediaPlayer;
    private boolean isMusicOn = true;

    @FXML
    private void initialize() {
        // Khởi tạo MediaPlayer với nhạc nền từ đường dẫn mới
        try {
            // Sử dụng đường dẫn tương đối trong thư mục resources
            Media sound = new Media(getClass().getResource("/assets/music/background_music.mp3").toURI().toString());
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
        Scene gameScene = GameSceneBuilder.buildGameScene();
        Main.mainStage.setScene(gameScene);
        Main.mainStage.setTitle("Bomberman Game");
        gameScene.getRoot().requestFocus();
    }

    @FXML
    private void handleMusicOn() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        musicOnButton.setVisible(false);
        musicOffButton.setVisible(true);
        isMusicOn = false;
        System.out.println("Music OFF");
    }

    @FXML
    private void handleMusicOff() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
        musicOnButton.setVisible(true);
        musicOffButton.setVisible(false);
        isMusicOn = true;
        System.out.println("Music ON");
    }
    public void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}
// Thêm phương thức để clean up MediaPlayer khi không cần thiết