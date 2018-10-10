package io.github.mike10004.crxtool;

import io.github.mike10004.crxtool.message.Crx3;

import java.util.List;

/**
 * Class that represents a CRX file header. This is the part of the file after the magic number and
 * one or more length fields but before the archive. This interface differs from {@link Crx3.CrxFileHeader}
 * in that it supports both CRX2 and CRX3 file headers.
 */
public interface CrxFileHeader {

    /**
     * Returns the key proofs associated with an algorithm.
     * @param algorithm the algorithm
     * @return the proof, possibly empty
     */
    List<AsymmetricKeyProof> getAsymmetricKeyProofs(String algorithm);

    /**
     * Gets the count of bytes in the packed binary form of this header instance.
     * @return the file header length, in bytes
     */
    int length();

}
