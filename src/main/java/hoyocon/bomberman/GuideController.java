package hoyocon.bomberman;

import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;

public class GuideController {
    @FXML private GridPane keyBox;
    @FXML private Button placeBomb;
    @FXML private Button itemBuffBomb;
    @FXML private Button itemBuffFlame;
    @FXML private Button itemBuffSpeed;
    @FXML private Button itemBuffFlamePass;
    @FXML private Button itemBuffLife;
    @FXML private Button nextRound;
    @FXML private Button ESC;
    @FXML private Label LabelOfkeyBox;
    @FXML private Label LabelOfplaceBomb;
    @FXML private Label LabelOfBuffBomb;
    @FXML private Label LabelOfBuffFlame;
    @FXML private Label LabelOfBuffSpeed;
    @FXML private Label LabelOfnextRound;
    @FXML private Label LabelOfESC;
    @FXML private Label LabelOfLife;
    @FXML private Label LabelOfFlamePass;

    @FXML
    private void initialize() {
        // Khi view được load, thiết lập lắng nghe phím
        javafx.application.Platform.runLater(() -> {
            // Lấy scene từ bất kỳ node nào
            Scene scene = Main.mainStage.getScene();  // hoặc sử dụng một node nếu bạn có
            if (scene != null) {
                scene.setOnKeyPressed(this::handleKeyPressed);
            }
        });
        setupHover(keyBox, LabelOfkeyBox, "Dùng các phím A, W, S, D để di chuyển nhân vật.");
        setupHover(placeBomb, LabelOfplaceBomb, "Cho phép người chơi đăt bom tại vị trí tương ứng, số lượng đặt cùng lúc có thể"
                + " thay đổi nhưng không thể đặt 2 quả bom cùng một vị trí.");
        setupHover(itemBuffBomb, LabelOfBuffBomb, "Tăng số lượng bom có thể đặt cùng lúc thêm 1, không cố định số lượng tối đa.");
        setupHover(itemBuffFlame, LabelOfBuffFlame, "Tăng phạm vi nổ của bom tăng trong 10s.");
        setupHover(itemBuffSpeed, LabelOfBuffSpeed, "Tăng tốc độ di chuyển của người chơi lên đáng kể trong 10s.");
        setupHover(nextRound, LabelOfnextRound, "Đích đến của hành trình.");
        setupHover(ESC, LabelOfESC, "Nhấn vào để bạn có thể quay lại các giao diện khi cần thiết");
        setupHover(itemBuffFlamePass, LabelOfFlamePass, "Cho phép người chơi có khả năng kháng hỏa trong 10s ngắn ngủi.");
        setupHover(itemBuffLife, LabelOfLife, "Hồi lại một sinh mạng cho người chơi.");
        LabelOfkeyBox.setVisible(false);
        LabelOfplaceBomb.setVisible(false);
        LabelOfBuffBomb.setVisible(false);
        LabelOfBuffFlame.setVisible(false);
        LabelOfBuffSpeed.setVisible(false);
        LabelOfnextRound.setVisible(false);
        LabelOfESC.setVisible(false);
        LabelOfLife.setVisible(false);
        LabelOfFlamePass.setVisible(false);
    }

    private void setupHover(Node node,Label label, String text) {
        node.setOnMouseEntered(e -> {
            label.setText(text);
            label.setVisible(true);
        });
        node.setOnMouseExited(e -> label.setVisible(false));

    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            try {
                Parent startView = FXMLLoader.load(getClass().getResource("/FXML/Start-view.fxml"));
                Scene scene = new Scene(startView, 1920, 1080);
                Stage stage = Main.mainStage;
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
