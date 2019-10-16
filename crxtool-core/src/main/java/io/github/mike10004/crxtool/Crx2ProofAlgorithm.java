package io.github.mike10004.crxtool;

public enum Crx2ProofAlgorithm implements CrxProofAlgorithm {

    sha1_with_rsa();

    private static final String KEY = "sha1_with_rsa";

    @Override
    public String crxFileHeaderKey() {
        return KEY;
    }
}
