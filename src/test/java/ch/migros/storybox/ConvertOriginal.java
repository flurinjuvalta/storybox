package ch.migros.storybox;

import java.io.*;

/**
 * @author Flurin Juvalta <flurin.juvalta@avelon.ch>
 */
public class ConvertOriginal {
    public static void main(String[] args) throws IOException {
        decode("M21");
        decode("M22");
        decode("M23");
        decode("M24");
        decode("M25");
        decode("M26");
        decode("M27");
        decode("M28");
        decode("M29");
        decode("M30");
    }

    private static void decode(final String name) throws IOException {
        final File source = new File("/home/flurin/Desktop/migros_box/migros_hoerbox_original/" + name + ".smp");
        final File target = new File("/home/flurin/git/storybox/src/dist/original/" + name + ".mp3");
        try (InputStream in = new BufferedInputStream(new FileInputStream(source));
             OutputStream out = new MigrosFileEncryptingStream(new BufferedOutputStream(new FileOutputStream(target)))) {

            long count = 0;
            long currentCount = 0;
            int n;
            byte[] buffer = new byte[4096];
            while (-1 != (n = in.read(buffer))) {
                out.write(buffer, 0, n);
                count += n;
            }
        }
    }
}
