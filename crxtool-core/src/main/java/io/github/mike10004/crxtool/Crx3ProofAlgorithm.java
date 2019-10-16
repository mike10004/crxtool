package io.github.mike10004.crxtool;

enum Crx3ProofAlgorithm implements CrxProofAlgorithm {

    sha256_with_rsa("SHA256", "RSA", "sha256_with_rsa"),
    sha256_with_ecdsa("SHA256", "RSA", "sha256_with_ecdsa");

    private final String javaHashFunction;
    private final String javaCryptoAlgorithm;
    final String fileHeaderKey;

    Crx3ProofAlgorithm(String javaHashFunction, String javaCryptoAlgorithm, String fileHeaderKey) {
        this.javaHashFunction = javaHashFunction;
        this.javaCryptoAlgorithm = javaCryptoAlgorithm;
        this.fileHeaderKey = fileHeaderKey;
    }

    public String javaHashFunction() {
        return javaHashFunction;
    }

    public String javaCryptoAlgorithm() {
        return javaCryptoAlgorithm;
    }

    @Override
    public String crxFileHeaderKey() {
        return fileHeaderKey;
    }
}
