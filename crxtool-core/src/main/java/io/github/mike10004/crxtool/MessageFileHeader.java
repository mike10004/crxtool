package io.github.mike10004.crxtool;

import com.google.common.collect.ImmutableList;
import io.github.mike10004.crxtool.message.Crx3;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

class MessageFileHeader implements CrxFileHeader {

    private final Crx3.CrxFileHeader message;

    public MessageFileHeader(Crx3.CrxFileHeader message) {
        this.message = requireNonNull(message);
    }

    @Override
    public List<AsymmetricKeyProof> getAsymmetricKeyProofs(String algorithm) {
        switch (algorithm) {
            case KeyPairs.ALGORITHM_SHA256_WITH_ECDSA:
                return message.getSha256WithEcdsaList().stream().map(this::convert).collect(ImmutableList.toImmutableList());
            case KeyPairs.ALGORITHM_SHA256_WITH_RSA:
                return message.getSha256WithRsaList().stream().map(this::convert).collect(ImmutableList.toImmutableList());
            default:
                return Collections.emptyList();
        }

    }

    protected AsymmetricKeyProof convert(Crx3.AsymmetricKeyProof source) {
        return BasicAsymmetricKeyProof.fromMessage(source);
    }

    @Override
    public int length() {
        return message.getSerializedSize();
    }
}
