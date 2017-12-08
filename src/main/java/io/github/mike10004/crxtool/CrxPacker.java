package io.github.mike10004.crxtool;

import com.google.common.io.ByteSource;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.zip.ZipOutputStream;

/**
 * Interface defining methods to pack Chrome extensions.
 */
public interface CrxPacker {


    default void packExtension(Path extensionDir, KeyPair keyPair, OutputStream output) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        packExtension(extensionDir, null, keyPair, output);
    }

    default void packExtension(Path extensionDir, @Nullable ZipConfig zipConfig, KeyPair keyPair, OutputStream output) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream(1024);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(zipBuffer)) {
            java.nio.file.Files.walkFileTree(extensionDir, new ZippingFileVisitor(extensionDir, zipOutputStream));
            if (zipConfig != null) {
                if (zipConfig.comment != null) {
                    zipOutputStream.setComment(zipConfig.comment);
                }
                if (zipConfig.method != null) {
                    zipOutputStream.setMethod(zipConfig.method);
                }
                if (zipConfig.level != null) {
                    zipOutputStream.setLevel(zipConfig.level);
                }
            }
        }
        byte[] zipBytes = zipBuffer.toByteArray();
        packExtension(ByteSource.wrap(zipBytes), keyPair, output);
    }

    /**
     * Packs an extension given the zip data and a key pair.
     * @param zipBytes byte source supplying zip data
     * @param keyPair key pair
     * @param output output stream
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
