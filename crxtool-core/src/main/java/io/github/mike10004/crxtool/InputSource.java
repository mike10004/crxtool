package io.github.mike10004.crxtool;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class that represents an input stream source.
 * This is a slimmed down version of Guava's {@code ByteSource}.
 * Use a {@code ByteSource::openStream} expression for quick
 * API conformance.
 *
 */
public interface InputSource {

    /**
     * Opens a byte stream from this source.
     * @return a new input stream
     * @throws IOException on I/O error
     */
    InputStream openStream() throws IOException;

    /**
     * Reads the entire stream into a byte array.
     * @return a new byte array containing all bytes in the stream
     * @throws IOException on I/O error
     */
    default byte[] read() throws IOException {
        try (InputStream in = openStream()) {
            return ByteStreams.toByteArray(in);
        }
    }

    /**
     * Copies all bytes from this source to the given destination.
     * @param out the destination
     * @return the number of bytes copied
     * @throws IOException on I/O error
     */
    @SuppressWarnings("UnusedReturnValue")
    default long copyTo(OutputStream out) throws IOException {
        try (InputStream in = openStream()) {
            return ByteStreams.copy(in, out);
        }
    }

    static InputSource wrap(byte[] data) {
        return new InputSources.BufferedInputSource(data);
    }

}
