package io.github.mike10004.crxtool;

import javax.annotation.Nullable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

import static java.util.Objects.requireNonNull;

class BasicSigner implements Signer {

    private final String hashFunction;
    private final String cryptoAlgorithm;
    @Nullable
    private final SecureRandom rng;

    public BasicSigner(String hashFunction, String cryptoAlgorithm) {
        this(hashFunction, cryptoAlgorithm, null);
    }

    public BasicSigner(String hashFunction, String cryptoAlgorithm, @Nullable SecureRandom rng) {
        this.hashFunction = requireNonNull(hashFunction);
        this.cryptoAlgorithm = requireNonNull(cryptoAlgorithm);
        this.rng = rng;
    }

    protected String getSignatureAlgorithm() {
        return String.format("%sWith%s", hashFunction, cryptoAlgorithm);
    }

    @Override
    public byte[] sign(byte[] input, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(getSignatureAlgorithm());
        if (rng == null) {
            signature.initSign(privateKey);
        } else {
            signature.initSign(privateKey, rng);
        }
        signature.update(input);
        return signature.sign();
    }
}
