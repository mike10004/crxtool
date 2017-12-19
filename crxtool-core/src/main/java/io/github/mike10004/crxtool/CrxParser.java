package io.github.mike10004.crxtool;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface defining methods for analyzing Chrome extension data.
 * A Chrome extension file is a zip file with a header prepended.
 * The {@link #parseMetadata(InputStream)}} reads that header from the stream,
 * so the remainder of the stream is a well-formed zip file. To read
 */
public interface CrxParser {

    /**
     * Parses the extension metadata from an input stream providing bytes
     * of an extension file.
     * @param crxInputStream the input stream
     * @return the metadata
     * @throws IOException if reading from the stream fails
     */
    CrxMetadata parseMetadata(InputStream crxInputStream) throws IOException;

    static CrxParser getDefault() {
        return BasicCrxParser.getDefaultInstance();
    }

    @SuppressWarnings("unused")
    class CrxParsingException extends IOException {
        public CrxParsingException(String message) {
            super(message);
        }

        public CrxParsingException(String message, Throwable cause) {
            super(message, cause);
        }

        public CrxParsingException(Throwable cause) {
            super(cause);
        }
    }
}
