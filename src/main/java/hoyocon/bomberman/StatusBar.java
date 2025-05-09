package hoyocon.bomberman;

import hoyocon.bomberman.Object.Player;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.animation.AnimationTimer;

import java.util.Map;

public class StatusBar extends VBox {
    private Player player;
    private HBox heartContainer;
    private HBox buffContainer;
    private Text levelText;

    public StatusBar(Player player) {
        if (player == null) {
            System.err.println("Error: Player is null in StatusBar constructor");
            this.player = new Player();
        } else {
            this.player = player;
        }

        this.setSpacing(10);
        this.setStyle("-fx-padding: 10; -fx-background-color: rgba(0, 0, 0, 0.5);");
        this.setAlignment(Pos.TOP_LEFT);  // Align everything to left

        levelText = new Text("Lvl: " + Player.getLevel());
        levelText.setStyle("-fx-font-size: 20; -fx-fill: white; -fx-font-family: 'Press Start 2P';");
        VBox levelBox = new VBox(levelText);
        levelBox.setAlignment(Pos.CENTER_LEFT);
        levelBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-padding: 5; -fx-border-color: white; -fx-border-width: 1;");
        levelBox.setMaxWidth(Double.MAX_VALUE);

        heartContainer = new HBox(5);
        heartContainer.setAlignment(Pos.CENTER_LEFT);
        updateHearts();

        buffContainer = new HBox(10);
        buffContainer.setAlignment(Pos.CENTER_LEFT);
        updateBuffIcons();

        this.getChildren().addAll(levelBox, heartContainer, buffContainer);

        System.out.println("StatusBar initialized with " + this.player.getLives() + " hearts");

        AnimationTimer statusUpdater = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateStatus();
            }
        };
        statusUpdater.start();
    }

    private void updateStatus() {
        updateHearts();
        updateBuffIcons();
        updateLevelText();
    }

    private void updateLevelText() {
        if (levelText != null) {
            levelText.setText("Lvl: " + player.getLevel());
        }
    }

    private void updateHearts() {
        int maxLives = 5;
        Image heartImage = getHeartImage();

        while (heartContainer.getChildren().size() < maxLives) {
            ImageView heartIcon = new ImageView(heartImage);
            heartIcon.setFitWidth(30);
            heartIcon.setFitHeight(30);
            heartContainer.getChildren().add(heartIcon);
        }

        int lives = player.getLives();
        for (int i = 0; i < maxLives; i++) {
            if (i < heartContainer.getChildren().size()) {
                ImageView heartIcon = (ImageView) heartContainer.getChildren().get(i);
                heartIcon.setVisible(i < lives);
            }
        }

        if (heartImage == null) {
            heartContainer.getChildren().clear();
            Text hpText = new Text("HP: " + player.getLives());
            hpText.setStyle("-fx-font-size: 20; -fx-fill: white;");
            heartContainer.getChildren().add(hpText);
            System.err.println("Failed to load heart image, falling back to text");
        }
    }

    private Image getHeartImage() {
        String imagePath = "/assets/textures/heart.png";
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                System.err.println("Failed to load heart image (image error): " + imagePath);
                return null;
            }
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load heart image: " + imagePath + " - " + e.getMessage());
            return null;
        }
    }

    private void updateBuffIcons() {
        buffContainer.getChildren().clear();
        Map<String, Long> activeBuffs = player.getActiveBuffs();
        if (activeBuffs == null) {
            System.err.println("Warning: activeBuffs is null in updateBuffIcons");
            return;
        }

        for (String buffType : activeBuffs.keySet()) {
            Image buffImage = getBuffImage(buffType);
            if (buffImage != null) {
                ImageView buffIcon = new ImageView(buffImage);
                buffIcon.setFitWidth(30);
                buffIcon.setFitHeight(30);
                buffContainer.getChildren().add(buffIcon);
            }
        }
    }

    private Image getBuffImage(String buffType) {
        if (buffType == null) {
            System.err.println("Warning: buffType is null in getBuffImage");
            return null;
        }

        String imagePath;
        switch (buffType.toLowerCase()) {
            case "speed":
                imagePath = "/assets/textures/powerup_speed.png";
                break;
            case "flamepass":
                imagePath = "/assets/textures/powerup_flamepass.png";
                break;
            case "flamerange":
                imagePath = "/assets/textures/powerup_flames.png";
                break;
            case "bomb":
                imagePath = "/assets/textures/powerup_bombs.png";
                break;
            default:
                System.err.println("Unknown buff type: " + buffType);
                return null;
        }

        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                System.err.println("Failed to load buff image (image error): " + imagePath);
                return null;
            }
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load buff image: " + imagePath + " - " + e.getMessage());
            return null;
        }
    }
}