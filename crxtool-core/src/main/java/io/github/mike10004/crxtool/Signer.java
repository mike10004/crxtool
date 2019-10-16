package io.github.mike10004.crxtool;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;

public interface Signer {
    byte[] sign(byte[] input, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException;
}
