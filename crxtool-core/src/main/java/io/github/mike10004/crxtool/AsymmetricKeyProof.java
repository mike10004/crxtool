package io.github.mike10004.crxtool;

import com.google.common.primitives.Ints;

/**
 * Interface that represents asymmetric key proof.
 */
public interface AsymmetricKeyProof {

    /**
     * Gets a base-64 encoding of the public key bytes.
     * @return the public key
     */
    String getPublicKeyBase64();

    /**
     * Gets the base-64 encoding of the signature.
     * @return the signature
     */
    String getSignatureBase64();

    /**
     * Gets the length of the public key byte array.
     * @return public key length
     */
    default int getPublicKeyLength() {
        return Ints.checkedCast(KeyPairs.countBase64EncodedBytes(getPublicKeyBase64()));
    }

    /**
     * Gets the length of the signature byte array.
     * @return signature length
     */
    default int getSignatureLength() {
        return Ints.checkedCast(KeyPairs.countBase64EncodedBytes(getSignatureBase64()));
    }

    /**
     * Gets the combined length of signature and public key byte arrays.
     * @return combined length
     */
    default int getCombinedLength() {
        return getPublicKeyLength() + getSignatureLength();
    }
}
