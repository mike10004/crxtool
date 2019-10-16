package io.github.mike10004.crxtool;

import java.util.Arrays;

public enum CrxProofAlgorithm {

    sha256_with_rsa("SHA256", "RSA", "sha256_with_rsa"),
    sha256_with_ecdsa("SHA256", "RSA", "sha256_with_ecdsa");

    private final String javaHashFunction;
    private final String javaCryptoAlgorithm;
    private final String fileHeaderKey;

    CrxProofAlgorithm(String javaHashFunction, String javaCryptoAlgorithm, String fileHeaderKey) {
        this.javaHashFunction = javaHashFunction;
        this.javaCryptoAlgorithm = javaCryptoAlgorithm;
        this.fileHeaderKey = fileHeaderKey;
    }

    public static Iterable<CrxProofAlgorithm> allKnown() {
        return Arrays.asList(values());
    }

    public String javaHashFunction() {
        return javaHashFunction;
    }

    public String javaCryptoAlgorithm() {
        return javaCryptoAlgorithm;
    }

    public String crxFileHeaderKey() {
        return fileHeaderKey;
    }
}
