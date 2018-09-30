package ch.migros.storybox;

import ch.migros.storybox.to.CharacterTO;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;

/**
 * @author Flurin Juvalta <flurin.juvalta@avelon.ch>
 */
public class FileUploadTask extends Task<UploadResult> {

    private static final String BOX_FILENAME_PATTERN = "M%02d";
    private static final String BOX_FILENAME_EXTENSION = ".smp";
    private static final String DEFAULT_BOX_DIR = "/media/flurin/MIGROS-BOX/";

    private static Logger logger = Logger.getLogger(StoryBoxUploader.class.getName());


    final CharacterTO character;
    final File file;

    public FileUploadTask(CharacterTO character, File file) {
        this.character = character;
        this.file = file;
    }

    @Override
    protected UploadResult call() {
        String filename = String.format(BOX_FILENAME_PATTERN, character.nr);
        final File outputDir = new File(DEFAULT_BOX_DIR);
        final File outputFile = new File(outputDir, filename + BOX_FILENAME_EXTENSION);
        final File outputTempFile = new File(outputFile.getAbsolutePath() + ".tmp");
        // wait for storybox
        if (!Files.isWritable(outputDir.toPath())) {
            this.updateMessage("Bitte die Storybox anhängen, damit die Datei kopiert werden kann");
            while (!Files.isWritable(outputDir.toPath())) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                }
                if (this.isCancelled()) {
                    return null;
                }
            }
        }

        // start uploading
        this.updateMessage("Kopieren...");

        long totalSize = file.length();
        final long flushSize = totalSize / 20;

        final File inputTempFile = new File("/tmp/" + filename + ".mp3");
        try (InputStream in = new BufferedInputStream(new FileInputStream(file));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(inputTempFile))) {
            int n;
            byte[] buffer = new byte[4096];
            while (!isCancelled() && -1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }

        this.updateMessage("Hochladen...");

        System.out.println("COPY " + inputTempFile.getAbsolutePath() + " TO " + outputFile.getAbsolutePath() + "...");
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputTempFile));
             OutputStream out = new MigrosFileEncryptingStream(new BufferedOutputStream(new FileOutputStream(outputTempFile)))) {

            long count = 0;
            long currentCount = 0;
            int n;
            byte[] buffer = new byte[4096];
            while (!isCancelled() && -1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
                count += n;
                currentCount += n;
                if (currentCount > flushSize) {
                    out.flush();
                    currentCount = 0;
                    updateProgress(count, totalSize);
                }
            }

            if (!isCancelled()) {
                Files.move(outputTempFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        outputTempFile.delete();
        inputTempFile.delete();
        return null;
    }
}
