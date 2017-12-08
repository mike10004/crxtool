package io.github.mike10004.crxtool;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Longs;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Static utility methods relating to key pairs.
 */
public class KeyPairs {

    private KeyPairs() {}

    public static KeyPair loadRsaKeyPairFromPrivateKeyBytes(byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
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

    public static KeyPair generateKeyPair(long seed) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        byte[] seedBytes = Longs.toByteArray(seed);
        SecureRandom random = new SecureRandom(seedBytes);
        keyGen.initialize(1024, random);
        return keyGen.generateKeyPair();
    }

}