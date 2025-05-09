package hoyocon.bomberman;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ConfirmationDialogController {
    @FXML
    private VBox dialogRoot;
    @FXML
    private Label titleLabel;
    @FXML
    private Label messageLabel;

    private Runnable onConfirmAction;

    public void setDialogData(String title, String message, Runnable onConfirm) {
        titleLabel.setText(title);
        messageLabel.setText(message);
        this.onConfirmAction = onConfirm;
    }

    @FXML
    private void onYesClicked() {
        closeDialog();
        if (onConfirmAction != null) {
            onConfirmAction.run();
        }
    }

    @FXML
    private void onNoClicked() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) dialogRoot.getScene().getWindow()).close();
    }
}