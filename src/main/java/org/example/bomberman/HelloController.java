package org.example.bomberman;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import java.io.IOException;

public class HelloController {
    @FXML
    private ImageView logoImageView;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button startButton;

    @FXML
    private void switchToLoadingView(ActionEvent event) throws IOException {
        /**
         * by NGUYEN BA AN.
         * 17/3/2025.
         * */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("loading-view.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        // Hiệu ứng mờ dần trước khi chuyển cảnh
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), stage.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        /*stage.setScene(new Scene(root));
        stage.show();*/
        fadeOut.setOnFinished(e -> {
            // Chuyển màn hình sau khi hiệu ứng fadeOut hoàn tất
            stage.setScene(new Scene(root));

            // Hiệu ứng sáng dần khi loading xuất hiện
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.8), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            /**
             * Tong Duc Hong Anh.
             * 17/3/2025.
             */
            fadeIn.setOnFinished(fadeEvent -> {
                // Thêm thời gian chờ 2 giây trước khi chuyển sang màn map1
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(pauseEvent -> switchToMap1(stage)); // Chuyển sang map1
                pause.play(); // Kích hoạt chờ
            });

            fadeIn.play();
        });

        fadeOut.play(); // Chạy hiệu ứng mờ dần

        System.out.println("Switching to loading view...");

    }

    /**
     * by NGUYEN BA AN
     * 17/3/2025
     */

    /*@FXML
    private Label welcomeText;
    @FXML
    private Label welcomeLabel;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void onWelcomeButtonClick() {
        welcomeLabel.setText("Happy International Women's Day!");
    }*/

    // Hiệu ứng fade-in cho giao diện hiện tại


    /**
     * Tong Duc Hong Anh.
     * 17/3/2025.
     */
    public void switchToMap1(Stage stage) {
        try {
            // Tải giao diện map1
            FXMLLoader loader = new FXMLLoader(getClass().getResource("map1.fxml"));
            Parent map1Root = loader.load();

            // Hiệu ứng chuyển cảnh cho giao diện map1
            FadeTransition fadeInMap1 = new FadeTransition(Duration.seconds(0.8), map1Root);
            fadeInMap1.setFromValue(0);
            fadeInMap1.setToValue(1);

            // Hiển thị giao diện map1
            stage.setScene(new Scene(map1Root));
            fadeInMap1.play(); // Bắt đầu hiệu ứng fade-in
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}