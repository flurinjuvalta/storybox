package ch.migros.storybox.components;

import ch.migros.storybox.to.CharacterTO;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Flurin Juvalta <flurin.juvalta@avelon.ch>
 */
public class CharacterButton extends Button {
    private static final String defaultStyle = "-fx-font: 12 arial; -fx-background-color: #f3f3f3; -fx-border-color: #f3f3f3; -fx-border-width: 3px";
    private static final String mouseEnteredStyle = "-fx-cursor: hand; -fx-font: 12 arial; -fx-background-color: #f3f3f3; -fx-border-color: #ff6600; -fx-border-width: 3px";

    private final CharacterTO storyCharacter;

    public CharacterButton(CharacterTO storyCharacter) {
        if (storyCharacter.externalImage) {
            try (InputStream in = new BufferedInputStream(new FileInputStream(storyCharacter.imageFile))) {
                this.setGraphic(new ImageView(new Image(in, 150, 150, true, true)));
            } catch (IOException ex) {
                System.out.println("File not found (" + storyCharacter.imageFile + ": " + ex.getMessage());
                try (InputStream in = CharacterButton.class.getResourceAsStream("/images/empty.png")) {
                    this.setGraphic(new ImageView(new Image(in, 150, 150, true, true)));
                } catch (IOException | NullPointerException e) {
                    System.out.println("Resource not found (" + storyCharacter.imageFile + ": " + e.getMessage());
                }

            }
        } else {
            try (InputStream in = CharacterButton.class.getResourceAsStream(storyCharacter.imageFile)) {
                this.setGraphic(new ImageView(new Image(in, 150, 150, true, true)));
            } catch (IOException | NullPointerException ex) {
                System.out.println("Resource not found (" + storyCharacter.imageFile + ": " + ex.getMessage());
            }
        }

        this.setStyle(defaultStyle);
        this.setOnMouseEntered(e -> this.setStyle(mouseEnteredStyle));
        this.setOnMouseExited(e -> this.setStyle(defaultStyle));

        this.storyCharacter = storyCharacter;
    }
}
