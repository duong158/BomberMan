package hoyocon.bomberman;

import hoyocon.bomberman.Object.Player;
import hoyocon.bomberman.Save.GameState;
import hoyocon.bomberman.Save.SaveManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MenuController {
    private Scene pausedScene;
    private static final String SCORE_FILE = "scores.txt";

    @FXML
    private javafx.scene.control.Button autoPlayButton;

    @FXML
    public void initialize() {
        autoPlayButton.setText("Auto Play");
    }

    @FXML
    private void onAutoPlayClicked(ActionEvent event) {
        // Đảo trạng thái auto‑play
        GameSceneBuilder.toggleAutoPlay();

        // Cập nhật nhãn theo trạng thái mới
        boolean enabled = getAutoPlayState();
        autoPlayButton.setText(enabled ? "Auto Play: ON" : "Auto Play: OFF");
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
    private void onContinueClicked(ActionEvent event) {
        if (pausedScene != null) {
            // Resume trực tiếp mà không load save
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(pausedScene);
            // Reset AI timer trước khi start loop
            if (GameSceneBuilder.playerAI != null) {
                GameSceneBuilder.playerAI.resetTimer();
            }

            if (GameSceneBuilder.gameLoop != null) {
                GameSceneBuilder.gameLoop.start();
            }
            pausedScene.getRoot().requestFocus();
        }
    }

    /**
     * Exit game.
     * */
    @FXML
    private void onExitClicked(ActionEvent event) {
        Platform.exit();
    }

    /**
     * Save Score game.
     * */
    @FXML
    private void onSaveScoreClicked(ActionEvent event) {
        int currentScore = 1234567;

        saveScoreToFile(currentScore);

        int highScore = readHighScore();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Save Score");
        alert.setHeaderText("Your score has been saved!");
        alert.setContentText(
                "🎯 Current Score: " + currentScore + "\n" +
                        "🏆 Highest Score: " + highScore
        );
        alert.showAndWait();
    }

    @FXML
    private void onStartClicked(ActionEvent event) {
        // Tạo và hiển thị màn hình game
        Scene gameScene = GameSceneBuilder.buildNewGameScene();
        Player.level = 1;
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(gameScene);
        stage.setTitle("Bomberman Game");
        gameScene.getRoot().requestFocus(); // Đảm bảo focus cho game
    }

    private void saveScoreToFile(int score) {
        try {
            Files.writeString(
                    Paths.get(SCORE_FILE),
                    score + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Error saving score: " + e.getMessage());
        }
    }

    private int readHighScore() {
        int max = 0;
        try {
            List<String> lines = Files.readAllLines(Paths.get(SCORE_FILE));
            for (String line : lines) {
                try {
                    int score = Integer.parseInt(line.trim());
                    if (score > max) max = score;
                } catch (NumberFormatException ignore) {
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading score file: " + e.getMessage());
        }
        return max;
    }

    public void setPausedScene(Scene scene) {
        this.pausedScene = scene;
    }
}