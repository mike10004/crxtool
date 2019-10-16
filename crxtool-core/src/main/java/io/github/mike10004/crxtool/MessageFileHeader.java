package io.github.mike10004.crxtool;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import io.github.mike10004.crxtool.message.Crx3;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

class MessageFileHeader implements CrxFileHeader {

    private final Crx3.CrxFileHeader message;

    public MessageFileHeader(Crx3.CrxFileHeader message) {
        this.message = requireNonNull(message);
    }

    @Override
    public List<AsymmetricKeyProof> getAsymmetricKeyProofs(CrxProofAlgorithm algorithm) {
        if (Crx3ProofAlgorithm.sha256_with_ecdsa.fileHeaderKey.equals(algorithm.crxFileHeaderKey())) {
            return message.getSha256WithEcdsaList().stream().map(this::convert).collect(ImmutableList.toImmutableList());
        }
        if (Crx3ProofAlgorithm.sha256_with_rsa.fileHeaderKey.equals(algorithm.crxFileHeaderKey())) {
            return message.getSha256WithRsaList().stream().map(this::convert).collect(ImmutableList.toImmutableList());
        }
        return Collections.emptyList();
    }

    private Stream<AsymmetricKeyProofContainer> streamProofContainers(CrxProofAlgorithm algorithm) {
        return getAsymmetricKeyProofs(algorithm).stream()
                .map(perAlgo -> AsymmetricKeyProofContainer.create(algorithm, perAlgo));
    }

    @Override
    public List<AsymmetricKeyProofContainer> getAllAsymmetricKeyProofs() {
        return Arrays.stream(Crx3ProofAlgorithm.values())
                .flatMap(this::streamProofContainers)
                .collect(Collectors.toList());
    }

    protected AsymmetricKeyProof convert(Crx3.AsymmetricKeyProof source) {
        return BasicAsymmetricKeyProof.fromMessage(source);
    }

    @Override
    public int numBytes() {
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
        MoreObjects.ToStringHelper h = MoreObjects.toStringHelper("Crx3FileHeader");
        for (CrxProofAlgorithm algorithm : Crx3ProofAlgorithm.values()) {
            int count = getAsymmetricKeyProofs(algorithm).size();
            h.add(String.format("%s.count", algorithm), count);
        }
        return h.toString();
    }
}
