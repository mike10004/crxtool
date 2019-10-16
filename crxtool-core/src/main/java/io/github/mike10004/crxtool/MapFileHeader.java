package io.github.mike10004.crxtool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class MapFileHeader implements CrxFileHeader {

    protected final ImmutableMultimap<CrxProofAlgorithm, AsymmetricKeyProof> proofs;

    public MapFileHeader(Multimap<CrxProofAlgorithm, AsymmetricKeyProof> proofs) {
        this.proofs = ImmutableMultimap.copyOf(proofs);
    }

    @Override
    public List<AsymmetricKeyProof> getAsymmetricKeyProofs(CrxProofAlgorithm algorithm) {
        return ImmutableList.copyOf(proofs.get(algorithm));
    }

    @Override
    public List<AsymmetricKeyProofContainer> getAllAsymmetricKeyProofs() {
        return proofs.entries().stream()
                .map(entry -> AsymmetricKeyProofContainer.create(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public int numBytes() {
        return proofs.values().stream().mapToInt(AsymmetricKeyProof::getCombinedLength).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapFileHeader)) return false;
        MapFileHeader that = (MapFileHeader) o;
        return Objects.equals(proofs, that.proofs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proofs);
    }

    @Override
    public String toString() {
        String classname = getClass().getSimpleName();
        if (classname.length() < 5) {
            classname = MapFileHeader.class.getSimpleName();
        }
        return String.format("%s{proofs=%s}", classname, proofs.keySet());
    }

}
