package io.github.mike10004.crxtool;

/**
 * Class representing Chrome extension metadata.
 *
 * <p><b>Version 2</b> (https://web.archive.org/web/20180114090616/https://developer.chrome.com/extensions/crx)
 * <pre>
 * Field              Type            Length          Value           Description
 * magic number       char[]          32 bits         Cr24            Chrome requires this constant at the beginning of every .crx package.
 * version            unsigned int    32 bits         2               The version of the *.crx file format used (currently 2).
 * public key length  unsigned int    32 bits         pubkey.length   The length of the RSA public key in bytes.
 * signature length   unsigned int    32 bits         sig.length      The length of the signature in bytes.
 * public key         byte[]          pubkey.length   pubkey.contents The contents of the author's RSA public key, formatted as an X509 SubjectPublicKeyInfo block.
 * signature          byte[]          sig.length      sig.contents    The signature of the ZIP content using the author's private key. The signature is created using the RSA algorithm with the SHA-1 hash function.
 * </pre>
 * <p><b>Version 3</b> (https://cs.chromium.org/chromium/src/components/crx_file/crx3.proto)
 * <pre>
 * A CRX₃ file is a binary file of the following format:
 *     [4 octets]: "Cr24", a magic number.
 *     [4 octets]: The version of the *.crx file format used (currently 3).
 *     [4 octets]: N, little-endian, the length of the header section.
 *     [N octets]: The header (the binary encoding of a CrxFileHeader).
 *     [M octets]: The ZIP archive.
 * </pre>
 */
public interface CrxMetadata {

    /**
     * Gets the file header.
     * @return the header
     */
    CrxFileHeader getFileHeader();

    /**
     * Extension ID, as computed from the public key.
     * @return the id
     */
    String getId();

    /**
     * Magic number of the file.
     * @return the magic number
     */
    String getMagicNumber();

    /**
     * Version of the CRX file format used.
     * @return the version
     */
    int getVersion();

}

