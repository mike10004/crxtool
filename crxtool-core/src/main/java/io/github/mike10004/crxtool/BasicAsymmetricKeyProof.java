package io.github.mike10004.crxtool;

import com.google.common.io.BaseEncoding;
import com.google.common.primitives.Ints;
import io.github.mike10004.crxtool.message.Crx3;

import java.util.Objects;

class BasicAsymmetricKeyProof implements AsymmetricKeyProof {

    private final String publicKeyBase64;
    private final String signatureBase64;
    private final int publicKeyLength, signatureLength;

    public BasicAsymmetricKeyProof(String publicKeyBase64, String signatureBase64) {
        this.publicKeyBase64 = publicKeyBase64;
        this.signatureBase64 = signatureBase64;
        this.publicKeyLength = Ints.checkedCast(KeyPairs.countBase64EncodedBytes(publicKeyBase64));
        this.signatureLength = Ints.checkedCast(KeyPairs.countBase64EncodedBytes(signatureBase64));
    }

    @Override
    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    @Override
    public String getSignatureBase64() {
        return signatureBase64;
    }

    @Override
    public int getPublicKeyLength() {
        return publicKeyLength;
    }

    @Override
    public int getSignatureLength() {
        return signatureLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasicAsymmetricKeyProof)) return false;
        BasicAsymmetricKeyProof that = (BasicAsymmetricKeyProof) o;
        return Objects.equals(publicKeyBase64, that.publicKeyBase64) &&
                Objects.equals(signatureBase64, that.signatureBase64);
    }

    @Override
    public int hashCode() {
        return Objects.hash(publicKeyBase64, signatureBase64);
    }

    private static final BaseEncoding BASE_64 = BaseEncoding.base64();

    public static AsymmetricKeyProof fromMessage(Crx3.AsymmetricKeyProof source) {
        String publicKeyBase64 = BASE_64.encode(source.getPublicKey().toByteArray());
        String signatureBase64 = BASE_64.encode(source.getSignature().toByteArray());
        return new BasicAsymmetricKeyProof(publicKeyBase64, signatureBase64);
    }
}
