package io.github.mike10004.crxtool;

import java.io.IOException;
import java.io.Reader;

/**
 * Interface of a service that parses a serialized private key.
 * File in {@code pem} format contain a base-64 encoding of the
 * key along with some garbage description lines.
 */
public interface PemParser {

    /**
     * Extracts the private key bytes from a character stream.
     * @param reader the character stream
     * @return the private key bytes
     * @throws IOException on I/O error
     */
    byte[] extractBytes(Reader reader) throws IOException;

    static PemParser getInstance() {
        return DefaultPemParser.getInstance();
    }
}

