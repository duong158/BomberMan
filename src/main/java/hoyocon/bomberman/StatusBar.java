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

    public StatusBar(Player player) {
        if (player == null) {
            System.err.println("Error: Player is null in StatusBar constructor");
            this.player = new Player();
        } else {
            this.player = player;
        }
        this.setSpacing(10);
        this.setStyle("-fx-padding: 10; -fx-background-color: rgba(0, 0, 0, 0.5);");
        this.setAlignment(Pos.TOP_RIGHT);

        // Heart Container for HP
        heartContainer = new HBox(5); // Spacing between hearts
        updateHearts();

        // Buff Container
        buffContainer = new HBox(10);
        updateBuffIcons();

        this.getChildren().addAll(heartContainer, buffContainer);
        System.out.println("StatusBar initialized with " + this.player.getLives() + " hearts at " + this.getTranslateX() + ", " + this.getTranslateY());

        // Update status bar periodically
        AnimationTimer statusUpdater = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateStatus();
//                System.out.println("StatusBar updated with " + player.getLives() + " hearts");
            }
        };
        statusUpdater.start();
    }

    private void updateStatus() {
        updateHearts();
        updateBuffIcons();
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
                if (i < lives) {
                    heartIcon.setVisible(true);
                } else {
                    heartIcon.setVisible(false);
                }
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