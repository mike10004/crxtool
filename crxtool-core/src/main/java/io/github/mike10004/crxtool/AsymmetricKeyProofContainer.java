package io.github.mike10004.crxtool;

import static java.util.Objects.requireNonNull;

/**
 * Interface that represents a container of a key proof.
 */
public interface AsymmetricKeyProofContainer {

    /**
     * Gets the algorithm of the proof in this container.
     * @return the algorithm
     */
    CrxProofAlgorithm algorithm();

    /**
     * Gets the proof.
     * @return the proof
     */
    AsymmetricKeyProof proof();

    static AsymmetricKeyProofContainer create(CrxProofAlgorithm algorithm, AsymmetricKeyProof proof) {
        requireNonNull(algorithm);
        requireNonNull(proof);
        return new AsymmetricKeyProofContainer() {
            @Override
            public CrxProofAlgorithm algorithm() {
                return algorithm;
            }

            @Override
            public AsymmetricKeyProof proof() {
                return proof;
            }

            @Override
            public String toString() {
                return String.format("AsymmetricKeyProofContainer#create{algorithm=%s,proof=%s}", algorithm, proof);
            }
        };
    }
}
