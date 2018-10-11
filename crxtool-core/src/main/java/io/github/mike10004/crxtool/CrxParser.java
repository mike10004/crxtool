package io.github.mike10004.crxtool;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface of a service that analyzes Chrome extension metadata.
 * A Chrome extension file is a zip file with a header prepended.
 * The {@link #parseMetadata(InputStream)}} reads that header from the stream,
 * so the remainder of the stream is a well-formed zip file. To read
 */
public interface CrxParser {

    /**
     * Parses the extension metadata from an input stream providing bytes of an extension file.
     * The input stream must be positioned at the first byte of the file. Upon completion,
     * the input strema will be positioned at the beginning of the portion of the file that
     * constitutes a zip archive. That is, the remainder of the stream can be parsed with a
     * {@link java.util.zip.ZipInputStream}.
     *
     * @param crxInputStream the input stream
     * @return the metadata
     * @throws IOException if reading from the stream fails
     */
    CrxMetadata parseMetadata(InputStream crxInputStream) throws IOException;

    /**
     * Gets a default (immutable) parser instance.
     * @return a parser
     */
    static CrxParser getDefault() {
        return BasicCrxParser.getDefaultInstance();
    }

}
