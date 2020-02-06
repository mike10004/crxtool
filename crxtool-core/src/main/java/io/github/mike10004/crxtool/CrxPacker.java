package io.github.mike10004.crxtool;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Interface of a service that packs a Chrome extension from source files.
 */
public interface CrxPacker {

    /**
     * Packs an extension from a given directory and key pair.
     * @param extensionDir the directory containing the extension files
     * @param keyPair the key pair to sign with
     * @param output the output stream
     * @throws IOException if reading, writing, or zipping data fails
     * @throws NoSuchAlgorithmException if RSA is not supported
     * @throws InvalidKeyException if thrown by {@link java.security.Signature#initSign(PrivateKey)}
     * @throws SignatureException if thrown by {@link java.security.Signature#update(byte[])} or {@link Signature#sign()}
     */
    default void packExtension(Path extensionDir, KeyPair keyPair, OutputStream output) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        packExtension(extensionDir, null, keyPair, output);
    }

    /**
     * Packs an extension from a given directory and key pair.
     * @param extensionDir the directory containing the extension files
     * @param keyPair the key pair to sign with
     * @param zipConfig options for zipping
     * @param output the output stream
     * @throws IOException if reading, writing, or zipping data fails
     * @throws NoSuchAlgorithmException if RSA is not supported
     * @throws InvalidKeyException if thrown by {@link java.security.Signature#initSign(PrivateKey)}
     * @throws SignatureException if thrown by {@link java.security.Signature#update(byte[])} or {@link Signature#sign()}
     */
    default void packExtension(Path extensionDir, @Nullable ZipConfig zipConfig, KeyPair keyPair, OutputStream output) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] zipBytes = Zipping.zipDirectory(extensionDir, zipConfig);
        packExtension(InputSource.wrap(zipBytes), keyPair, output);
    }

    /**
     * Packs an extension given the zip data and a key pair.
     * @param zipBytes byte source supplying zip data
     * @param keyPair key pair
     * @param output output stream
     * @throws IOException if reading or writing data fails
     * @throws NoSuchAlgorithmException if RSA is not supported
     * @throws InvalidKeyException if thrown by {@link java.security.Signature#initSign(PrivateKey)}
     * @throws SignatureException if thrown by {@link java.security.Signature#update(byte[])} or {@link Signature#sign()}
     * @see KeyPairs#loadRsaKeyPairFromPrivateKeyBytes(byte[])
     */
    void packExtension(InputSource zipBytes, KeyPair keyPair, OutputStream output) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException;

    /**
     * Gets a default instance. This currently returns a packer that packs
     * a version 3 CRX.
     * @return a default instance
     */
    static CrxPacker getDefault() {
        return getPackerInstance(CrxVersion.CRX3);
    }

    /**
     * Gets a packer that packs in the specified version.
     * Only version 2 is currently supported.
     * @param version the version
     * @return the packer
     * @deprecated use {@link #getPackerInstance(CrxVersion)} instead
     */
    @Deprecated
    static CrxPacker getPackerInstance(int version) {
        return getPackerInstance(CrxVersion.fromIdentifier(version));
    }

    static CrxPacker getPackerInstance(CrxVersion version) {
        switch (version) {
            case CRX3:
                return Crx3Packer.getDefaultInstance();
            case CRX2:
                throw new IllegalArgumentException("version 2 is no longer supported (as of crxtool 0.16)");
            default:
                throw new IllegalArgumentException(String.format("only version 3 is supported, not %s", version));
        }
    }

    CrxVersion getCrxVersion();
}
