package io.github.mike10004.crxtool;

import java.io.IOException;
import java.io.Reader;

/**
 * Interface of a service that parses a serialized private key.
 * File in {@code pem} format contain a base-64 encoding of the
 * key along with some garbage description lines.
 * Requires a pem file with exactly one key in it, as opposed to
 * a pem file that contains both private and public key explicitly.
 * <p>Note that while this class will parse the key bytes out of any pem file,
 * the method {@link KeyPairs#loadRsaPrivateKeyFromKeyBytes(byte[])} expects
 * the key to be in PKCS8 format.</p>
 */
public interface PemParser {

    /**
     * Extracts the private key bytes from a character stream.
     * @param reader the character stream
     * @return the private key bytes
     * @throws IOException on I/O error
     */
    byte[] extractBytes(Reader reader) throws IOException;

    /**
     * Gets a default implementation instance.
     * @return a parser instance
     */
    static PemParser getInstance() {
        return DefaultPemParser.getInstance();
    }
}

