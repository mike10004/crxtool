package io.github.mike10004.crxtool;

import com.google.common.io.ByteSource;

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
        packExtension(ByteSource.wrap(zipBytes), keyPair, output);
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
    void packExtension(ByteSource zipBytes, KeyPair keyPair, OutputStream output) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException;

    /**
     * Gets a default instance. This currently returns a packer that packs
     * a version 2 CRX, but that may change in a future release.
     * @return a default instance
     */
    static CrxPacker getDefault() {
        return getPackerInstance(CrxVersion.CRX2);
    }

    /**
     * Gets a packer that packs in the specified version.
     * Only version 2 is currently supported.
     * @param version the version
     * @return the packer
     * @deprecated use {@link }
     */
    @Deprecated
    static CrxPacker getPackerInstance(int version) {
        return getPackerInstance(CrxVersion.fromIdentifier(version));
    }

    static CrxPacker getPackerInstance(CrxVersion version) {
        switch (version) {
            case CRX2:
                return Crx2Packer.getDefaultInstance();
            case CRX3:
                throw new UnsupportedOperationException("CRX3 is not yet supported");
            default:
                throw new IllegalArgumentException(String.format("only version 2 is supported, not %s", version));
        }
    }

    CrxVersion getCrxVersion();
}
