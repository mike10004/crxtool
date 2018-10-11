package io.github.mike10004.crxtool;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import io.github.mike10004.crxtool.message.Crx3;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

class MessageFileHeader implements CrxFileHeader {

    private final Crx3.CrxFileHeader message;

    public MessageFileHeader(Crx3.CrxFileHeader message) {
        this.message = requireNonNull(message);
    }

    @Override
    public List<AsymmetricKeyProof> getAsymmetricKeyProofs(String algorithm) {
        switch (algorithm) {
            case MapFileHeader.ALGORITHM_SHA256_WITH_ECDSA:
                return message.getSha256WithEcdsaList().stream().map(this::convert).collect(ImmutableList.toImmutableList());
            case MapFileHeader.ALGORITHM_SHA256_WITH_RSA:
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageFileHeader)) return false;
        MessageFileHeader that = (MessageFileHeader) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return toString(message);
    }

    private static String toString(Crx3.CrxFileHeader message) {
        return MoreObjects.toStringHelper(message)
                .add(String.format("%s.count", MapFileHeader.ALGORITHM_SHA256_WITH_RSA), message.getSha256WithRsaCount())
                .add(String.format("%s.count", MapFileHeader.ALGORITHM_SHA256_WITH_ECDSA), message.getSha256WithEcdsaCount())
                .toString();
    }
}
