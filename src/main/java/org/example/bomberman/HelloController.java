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
            fadeIn.play();
        });

        fadeOut.play(); // Chạy hiệu ứng mờ dần

        System.out.println("Switching to loading view...");

    }
    /**
     * by NGUYEN BA AN
     * 17/3/2025
     * */

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
}