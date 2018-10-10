package io.github.mike10004.crxtool;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Objects;

class MapFileHeader implements CrxFileHeader {

    public static final String ALGORITHM_SHA256_WITH_RSA = "sha256_with_rsa";
    public static final String ALGORITHM_SHA256_WITH_ECDSA = "sha256_with_ecdsa";
    
    protected final ImmutableMultimap<String, AsymmetricKeyProof> proofs;

    MapFileHeader(Multimap<String, AsymmetricKeyProof> proofs) {
        this.proofs = ImmutableMultimap.copyOf(proofs);
    }

    @Override
    public List<AsymmetricKeyProof> getAsymmetricKeyProofs(String algorithm) {
        return ImmutableList.copyOf(proofs.get(algorithm));
    }

    @Override
    public int length() {
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
