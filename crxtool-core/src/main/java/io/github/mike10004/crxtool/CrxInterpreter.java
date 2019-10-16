package io.github.mike10004.crxtool;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface of a service that parses metadata from an input stream.
 * {@link BasicCrxParser} parses the version and then defers to
 * an implementation of this interface to parse the rest of the metadata.
 */
public interface CrxInterpreter {

    /**
     * Parses the metadata that follows the version entry in the packed binary file format.
     * @param crxInput input stream positioned immediately after the version
     * @return a metadata object
     * @throws IOException if I/O fails
     */
    CrxMetadata parseMetadataAfterVersion(InputStream crxInput, ParsingState state) throws IOException;

    /**
     * Exception thrown if the version specified by a CRX file is not supported.
     */
    class UnsupportedCrxVersionException extends CrxParsingException {

        public UnsupportedCrxVersionException(String message) {
            super(message);
        }
    }
}
