package ch.migros.storybox;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Flurin Juvalta <flurin.juvalta@avelon.ch>
 */
public class MigrosFileEncryptingStream extends OutputStream {
    public static final byte key = 0x66; // f
    final OutputStream out;

    public MigrosFileEncryptingStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b ^ key);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }
}
