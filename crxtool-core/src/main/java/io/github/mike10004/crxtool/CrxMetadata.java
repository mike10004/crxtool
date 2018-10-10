package io.github.mike10004.crxtool;

import com.google.common.collect.ImmutableMultimap;

import static java.util.Objects.requireNonNull;

/**
 * Class representing Chrome extension metadata.
 *
 * <p>Warning: an interface with the same methods will replace this class in a
 * future version. This change will be source-compatible with clients that use
 * only the methods. It will not be binary-compatible with anything.</p>
 * <h5>Version 2 (https://web.archive.org/web/20180114090616/https://developer.chrome.com/extensions/crx)</h5>
 * <pre>
 * Field              Type            Length          Value           Description
 * magic number       char[]          32 bits         Cr24            Chrome requires this constant at the beginning of every .crx package.
 * version            unsigned int    32 bits         2               The version of the *.crx file format used (currently 2).
 * public key length  unsigned int    32 bits         pubkey.length   The length of the RSA public key in bytes.
 * signature length   unsigned int    32 bits         sig.length      The length of the signature in bytes.
 * public key         byte[]          pubkey.length   pubkey.contents The contents of the author's RSA public key, formatted as an X509 SubjectPublicKeyInfo block.
 * signature          byte[]          sig.length      sig.contents    The signature of the ZIP content using the author's private key. The signature is created using the RSA algorithm with the SHA-1 hash function.
 * </pre>
 * <h5>Version 3 (https://cs.chromium.org/chromium/src/components/crx_file/crx3.proto)</h5>
 * // A CRX₃ file is a binary file of the following format:
 * // [4 octets]: "Cr24", a magic number.
 * // [4 octets]: The version of the *.crx file format used (currently 3).
 * // [4 octets]: N, little-endian, the length of the header section.
 * // [N octets]: The header (the binary encoding of a CrxFileHeader).
 * // [M octets]: The ZIP archive.
 */
@SuppressWarnings("deprecation")
public final class CrxMetadata {

    /**
     * @deprecated use {@link #getId()}; an interface of the same name will replace this class in a future version
     */
    @Deprecated
    public final String id;

    /**
     * @deprecated use {@link #getMagicNumber()}; an interface of the same name will replace this class in a future version
     */
    @Deprecated
    public final String magicNumber;

    /**
     * @deprecated use {@link #getVersion()}; an interface of the same name will replace this class in a future version
     */
    @Deprecated
    public final int version;

    /**
     * Length of the developer RSA public key in bytes.
     * @deprecated refers to the {@code sha256_with_rsa} key proof; get that from {@link #getFileHeader()} instead
     */
    @Deprecated
    public final int pubkeyLength;

    /**
     * Developer RSA public key in base-64 encoding.
     * @deprecated refers to the {@code sha256_with_rsa} key proof; get that from {@link #getFileHeader()} instead
     */
    @Deprecated
    public final String pubkeyBase64;

    /**
     * Length of the zip content signature in bytes.
     * @deprecated refers to the {@code sha256_with_rsa} key proof; get that from {@link #getFileHeader()} instead
     */
    @Deprecated
    public final int signatureLength;

    /**
     * ZIP content signature using the developer's private key.
     * @deprecated refers to the {@code sha256_with_rsa} key proof; get that from {@link #getFileHeader()} instead
     */
    @Deprecated
    public final String signatureBase64;

    private final CrxFileHeader fileHeader;

    CrxMetadata(String magicNumber, int version, CrxFileHeader fileHeader, String id) {
        this.magicNumber = requireNonNull(magicNumber);
        this.version = version;
        this.fileHeader = requireNonNull(fileHeader);
        this.id = requireNonNull(id);
        // legacy: pubkey/signature fields refer to CRX2 sha256_with_rsa proof
        AsymmetricKeyProof asymmetricKeyProof = fileHeader.getAsymmetricKeyProofs(MapFileHeader.ALGORITHM_SHA256_WITH_RSA).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("file header does not contain sha256_with_rsa asymmetric key proof"));
        this.pubkeyLength = asymmetricKeyProof.getPublicKeyLength();
        this.pubkeyBase64 = asymmetricKeyProof.getPublicKeyBase64();
        this.signatureLength = asymmetricKeyProof.getSignatureLength();
        this.signatureBase64 = asymmetricKeyProof.getSignatureBase64();
    }

    @Deprecated
    CrxMetadata(String magicNumber, int version, int pubkeyLength, String pubkeyBase64, int signatureLength, String signatureBase64, String id) {
        this(magicNumber, version, createCrx2FileHeader(pubkeyBase64, signatureBase64), id);
    }

    public CrxFileHeader getFileHeader() {
        return fileHeader;
    }

    private static CrxFileHeader createCrx2FileHeader(String pubkeyBase64, String signatureBase64) {
        return new MapFileHeader(ImmutableMultimap.of(MapFileHeader.ALGORITHM_SHA256_WITH_RSA, createCrx2Proof(pubkeyBase64, signatureBase64)));
    }

    private static AsymmetricKeyProof createCrx2Proof(String pubkeyBase64, String signatureBase64) {
        return new BasicAsymmetricKeyProof(pubkeyBase64, signatureBase64);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrxMetadata that = (CrxMetadata) o;

        if (getVersion() != that.getVersion()) return false;
        if (pubkeyLength != that.pubkeyLength) return false;
        if (signatureLength != that.signatureLength) return false;
        if (getMagicNumber() != null ? !getMagicNumber().equals(that.getMagicNumber()) : that.getMagicNumber() != null) return false;
        if (pubkeyBase64 != null ? !pubkeyBase64.equals(that.pubkeyBase64) : that.pubkeyBase64 != null) return false;
        if (signatureBase64 != null ? !signatureBase64.equals(that.signatureBase64) : that.signatureBase64 != null)
            return false;
        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        int result = getMagicNumber() != null ? getMagicNumber().hashCode() : 0;
        result = 31 * result + getVersion();
        result = 31 * result + pubkeyLength;
        result = 31 * result + (pubkeyBase64 != null ? pubkeyBase64.hashCode() : 0);
        result = 31 * result + signatureLength;
        result = 31 * result + (signatureBase64 != null ? signatureBase64.hashCode() : 0);
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CrxMetadata{" +
                "id='" + getId() + '\'' +
                ", magicNumber='" + getMagicNumber() + '\'' +
                ", version=" + getVersion() +
                ", pubkeyLength=" + pubkeyLength +
                ", pubkeyBase64='" + CommonsLang3StringUtils.abbreviate(pubkeyBase64, 16) + '\'' +
                ", signatureLength=" + signatureLength +
                ", signatureBase64='" + CommonsLang3StringUtils.abbreviate(signatureBase64, 16) + '\'' +
                '}';
    }

    /**
     * Extension ID, as computed from the public key.
     */
    public String getId() {
        return id;
    }

    /**
     * Magic number of the file.
     */
    public String getMagicNumber() {
        return magicNumber;
    }

    /**
     * Version of the CRX file format used.
     */
    public int getVersion() {
        return version;
    }
}
