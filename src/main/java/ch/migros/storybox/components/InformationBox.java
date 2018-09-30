package ch.migros.storybox.components;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * @author Flurin Juvalta <flurin.juvalta@avelon.ch>
 */
public class InformationBox extends HBox {

    public InformationBox() {
        this.setPadding(new Insets(12, 12, 12, 12));
        this.setStyle("-fx-padding: 10;" +
                "-fx-border-style: solid;" +
                "-fx-border-width: 2;" +
                "-fx-border-insets: 5;" +
                "-fx-border-color: #ff6600;");

        this.setOnMouseClicked(e -> setDefaultText());

        this.setDefaultText();
    }

    private void setDefaultText() {
        final Text text1 = new Text("Klicke auf die gewünschte Figur um eine mp3 Datei hochzuladen.\n");
        final Text text2 = new Text("Achtung");
        text2.setStyle("-fx-font-weight: bold;");
        final Text text3 = new Text(": Die Datei auf der Box wird überschrieben");
        final TextFlow informationText = new TextFlow(text1, text2, text3);
        this.getChildren().clear();
        this.getChildren().add(informationText);
    }
}
