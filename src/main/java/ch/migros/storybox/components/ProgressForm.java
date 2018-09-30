package ch.migros.storybox.components;

import javafx.concurrent.Task;
import javafx.geometry.HorizontalDirection;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Flurin Juvalta <flurin.juvalta@avelon.ch>
 */
public class ProgressForm {
    private final Stage dialogStage;
    private final ProgressBar pb = new ProgressBar();
    private final Button cancelButton;
    private final Label status;

    public ProgressForm(final Stage owner, final String label) {
        dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.setResizable(false);
        dialogStage.initModality(Modality.WINDOW_MODAL);

        final Label labelNode = new Label(label);

        //pb.setProgress(-1F);
        pb.setStyle("-fx-accent: #ff6600; -fx-min-width: 500;");
        pb.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

        final VBox hb = new VBox();
        hb.setSpacing(5);
        hb.setAlignment(Pos.TOP_LEFT);
        hb.getChildren().addAll(labelNode, pb);

        cancelButton = new Button("Abbrechen");
        HBox buttons = new HBox(cancelButton);
        buttons.setAlignment(Pos.BOTTOM_RIGHT);

        status = new Label();

        VBox vb = new VBox(hb, status, buttons);
        vb.setPadding(new Insets(12, 12, 12, 12));

        dialogStage.setScene(new Scene(vb));

        // Calculate the center position of the parent Stage
        double centerXPosition = owner.getX() + owner.getWidth() / 2d;
        double centerYPosition = owner.getY() + owner.getHeight() / 2d;

        // Hide the pop-up stage before it is shown and becomes relocated
        dialogStage.setOnShowing(ev -> dialogStage.hide());

        // Relocate the pop-up Stage
        dialogStage.setOnShown(ev -> {
            dialogStage.setX(centerXPosition - dialogStage.getWidth() / 2d);
            dialogStage.setY(centerYPosition - dialogStage.getHeight() / 2d);
            dialogStage.show();
        });

    }

    public void activateProgressBar(final Task<?> task) {
        pb.progressProperty().bind(task.progressProperty());
        status.textProperty().bind(task.messageProperty());
        dialogStage.show();
        cancelButton.setOnAction(e -> task.cancel());
    }

    public Stage getDialogStage() {
        return dialogStage;
    }
}
