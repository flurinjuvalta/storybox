package ch.migros.storybox;

import ch.migros.storybox.components.CharacterButton;
import ch.migros.storybox.components.InformationBox;
import ch.migros.storybox.components.ProgressForm;
import ch.migros.storybox.to.CharacterTO;
import ch.migros.storybox.to.CharacterTabTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author Flurin Juvalta <flurin.juvalta@avelon.ch>
 */

public final class StoryBoxUploader extends Application {
    private static final String DEFAULT_CHARACTER_DEFINITION_FILE = "/default-characters.json";
    private static final String CUSTOM_CHARACTER_DEFINITION_FILE = "/.storybox/custom-characters.json";
    private static final String ORIGINAL_MP3_LOCATION = "/opt/storybox/original/";
    private static final String MP3_FILENAME_PATTERN = "M%02d.mp3";

    private final static FileChooser mp3FileChooser = new FileChooser();
    private static MediaPlayer mediaPlayer;
    private File initialDir = null;


    static {
        mp3FileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("mp3", "*.mp3"));
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(final Stage stage) {
        List<CharacterTabTO> characterTabTOS = readCharacterTabs();

        if (!characterTabTOS.isEmpty()) {
            final TabPane tabPane = new TabPane(createCharacterTabs(stage, characterTabTOS));
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            tabPane.setTabMinWidth(150);

            final InformationBox informationBox = new InformationBox();

            final VBox rootBox = new VBox(tabPane, informationBox);
            stage.setScene(new Scene(rootBox));
        } else {
            final Label label = new Label("Keine Figuren gefunden");
            label.setPadding(new Insets(12, 12, 12, 12));
            stage.setScene(new Scene(label));
        }


        stage.setTitle("Migros Hörbox");
        stage.setResizable(false);
        stage.show();
    }

    private List<CharacterTabTO> readCharacterTabs() {
        final List<CharacterTabTO> tabs = new ArrayList<>();
        try {
            final ObjectMapper mapper = new ObjectMapper();
            final CharacterTabTO[] characterTabTOS = mapper.readValue(StoryBoxUploader.class.getResource(DEFAULT_CHARACTER_DEFINITION_FILE), CharacterTabTO[].class);
            tabs.addAll(Arrays.asList(characterTabTOS));

            final File customCharacterFile = new File(System.getProperty("user.home") + CUSTOM_CHARACTER_DEFINITION_FILE);
            if (customCharacterFile.isFile()) {
                final CharacterTabTO[] customTabs = mapper.readValue(customCharacterFile, CharacterTabTO[].class);

                tabs.addAll(Arrays.asList(customTabs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tabs;
    }

    private Tab[] createCharacterTabs(final Stage stage, final List<CharacterTabTO> characterTabsTo) {
        return characterTabsTo.stream().map(tabTo -> {
            final GridPane buttons = new GridPane();
            int col = 0;
            int row = 0;
            for (CharacterTO character : tabTo.characters) {
                addCharacterButton(stage, buttons, character, col, row);
                col++;
                if (col == 4) {
                    col = 0;
                    row++;
                }
            }

            final Tab tab = new Tab();
            tab.setText(tabTo.name);
            tab.setContent(buttons);
            return tab;
        }).toArray(Tab[]::new);
    }

    private void addCharacterButton(final Stage stage, final GridPane gridPane, CharacterTO character, int columnIndex, int rowIndex) {
        final CharacterButton button = new CharacterButton(character);
        GridPane.setConstraints(button, columnIndex, rowIndex);
        gridPane.getChildren().add(button);

        button.setOnAction(e -> {
            if (initialDir != null) {
                mp3FileChooser.setInitialDirectory(initialDir);
            }
            uploadFile_Task(stage, character, mp3FileChooser.showOpenDialog(stage));
        });

        final File originalFile = new File(ORIGINAL_MP3_LOCATION + String.format(MP3_FILENAME_PATTERN, character.nr));

        if (originalFile.isFile()) {
            final ContextMenu cm = new ContextMenu();
            MenuItem cmItem1 = new MenuItem("Original hochladen");
            cmItem1.setOnAction(e -> uploadFile_Task(stage, character, originalFile));

            MenuItem cmItem2 = new MenuItem("Original hören");
            cmItem2.setOnAction(e -> {
                Media hit = new Media(originalFile.toURI().toString());
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                }
                mediaPlayer = new MediaPlayer(hit);
                mediaPlayer.play();
            });



            cm.getItems().addAll(cmItem1, cmItem2);
            button.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.SECONDARY)
                    cm.show(button, e.getScreenX(), e.getScreenY());
            });


        }
    }

    private void uploadFile_Task(final Stage stage, final CharacterTO character, File file) {
        if (file != null && file.isFile()) {
            initialDir = file.getParentFile();
            final Task<UploadResult> task = new FileUploadTask(character, file);

            final ProgressForm pForm = new ProgressForm(stage, file.getName() + " wird für " + character.name + " auf die Box geladen");
            // binds progress of progress bars to progress of task:
            pForm.activateProgressBar(task);
            task.setOnSucceeded(event -> pForm.getDialogStage().close());
            task.setOnCancelled(event -> pForm.getDialogStage().close());
            task.setOnFailed(event -> pForm.getDialogStage().close());
            pForm.getDialogStage().show();

            final Thread thread = new Thread(task);
            thread.start();
        }
    }
}
