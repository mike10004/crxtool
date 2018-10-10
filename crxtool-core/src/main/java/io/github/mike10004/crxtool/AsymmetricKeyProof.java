package io.github.mike10004.crxtool;

import com.google.common.primitives.Ints;

public interface AsymmetricKeyProof {

    String getPublicKeyBase64();

    String getSignatureBase64();

    default int getPublicKeyLength() {
        return Ints.checkedCast(KeyPairs.countBase64EncodedBytes(getPublicKeyBase64()));
    }

    default int getSignatureLength() {
        return Ints.checkedCast(KeyPairs.countBase64EncodedBytes(getSignatureBase64()));
    }

    default int getCombinedLength() {
        return getPublicKeyLength() + getSignatureLength();
    }
}
