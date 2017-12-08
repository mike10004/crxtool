package io.github.mike10004.crxtool;

import com.google.common.io.ByteSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
        ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream(1024);
        java.nio.file.Files.walkFileTree(extensionDir, new ZippingFileVisitor(extensionDir, new ZipOutputStream(output)));
        zipBuffer.flush();
        byte[] zipBytes = zipBuffer.toByteArray();
        packExtension(ByteSource.wrap(zipBytes), keyPair, output);
    }

    /**
     * Packs an extension given the zip data and a key pair.
     * @param zipBytes
     * @param keyPair
     * @param output
     * @see KeyPairs#loadRsaKeyPairFromPrivateKeyBytes(byte[])
     */
    void packExtension(ByteSource zipBytes, KeyPair keyPair, OutputStream output) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException;

    static CrxPacker getDefault() {
        return BasicCrxPacker.getDefaultInstance();
    }
}
