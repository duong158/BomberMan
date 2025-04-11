package hoyocon.bomberman.Object;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MenuController {
    private static final String SCORE_FILE = "scores.txt";

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

}
