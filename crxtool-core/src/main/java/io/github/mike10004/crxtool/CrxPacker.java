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
 * Interface defining methods to pack Chrome extensions.
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
     * Gets a default instance.
     * @return a default instance
     */
    static CrxPacker getDefault() {
        return BasicCrxPacker.getDefaultInstance();
    }
}
