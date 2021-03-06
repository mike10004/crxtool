package io.github.mike10004.crxtool;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Static utility methods relating to key pairs.
 *
 * <p>This class is public because it is used by the Maven plugin.
 */
public class KeyPairs {

    private KeyPairs() {}

    public static PrivateKey loadRsaPrivateKeyFromKeyBytes(byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Loads an RSA key pair from a byte array that constitutes the private key.
     * @param privateKeyBytes the private key bytes
     * @return the key pair
     * @throws NoSuchAlgorithmException if RSA is not supported
     * @throws InvalidKeySpecException if thrown by {@link KeyFactory#generatePrivate(KeySpec)} on a {@link PKCS8EncodedKeySpec} instance
     */
    public static KeyPair loadRsaKeyPairFromPrivateKeyBytes(byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PrivateKey privateKey = loadRsaPrivateKeyFromKeyBytes(privateKeyBytes);
        PublicKey publicKey = extractPublicKey(privateKey);
        return new KeyPair(publicKey, privateKey);
    }

    // https://stackoverflow.com/questions/8434428/get-public-key-from-private-in-java
    @VisibleForTesting
    static PublicKey extractPublicKey(PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPrivateCrtKey privk = (RSAPrivateCrtKey)privateKey;
        RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        return publicKey;
    }


    // https://stackoverflow.com/a/24689684/2657036
    public static KeyPair generateRsaKeyPair(SecureRandom random) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024, random);
        return keyGen.generateKeyPair();
    }

    static long countBase64EncodedBytes(String base64) {
        long len;
        try (StringReader reader = new StringReader(base64)) {
            len = ByteStreams.copy(BaseEncoding.base64().decodingStream(reader), ByteStreams.nullOutputStream());
        } catch (IOException e) {
            throw new AssertionError("IOException shouldn't be thrown by memory-only input streams");
        }
        return len;
    }

    public static String encodePublicKeyBase64(KeyPair keyPair) {
        return BaseEncoding.base64().encode(keyPair.getPublic().getEncoded());
    }
}
