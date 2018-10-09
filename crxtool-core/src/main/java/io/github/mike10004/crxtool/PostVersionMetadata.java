package io.github.mike10004.crxtool;

class PostVersionMetadata {

    /**
     * Length of the developer RSA public key in bytes.
     */
    public final int pubkeyLength;

    /**
     * Developer RSA public key in base-64 encoding.
     */
    public final String pubkeyBase64;

    /**
     * Length of the zip content signature in bytes.
     */
    public final int signatureLength;

    /**
     * ZIP content signature using the developer's private key.
     */
    public final String signatureBase64;

    /**
     * Extension ID, as computed from the public key.
     */
    public final String id;

    public PostVersionMetadata(int pubkeyLength, String pubkeyBase64, int signatureLength, String signatureBase64, String id) {
        this.pubkeyLength = pubkeyLength;
        this.pubkeyBase64 = pubkeyBase64;
        this.signatureLength = signatureLength;
        this.signatureBase64 = signatureBase64;
        this.id = id;
    }
}
