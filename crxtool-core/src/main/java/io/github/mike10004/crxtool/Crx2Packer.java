package io.github.mike10004.crxtool;

import com.google.common.io.ByteSource;
import com.google.common.io.LittleEndianDataOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Implementation that packs a Chrome extension in CRX2 format.
 */
public class Crx2Packer implements CrxPacker {

    private static final Crx2Packer DEFAULT_INSTANCE = new Crx2Packer();

    private static final String MAGIC_NUMBER = CrxPackers.MAGIC_NUMBER;
    static final CrxVersion CRX_VERSION = CrxVersion.CRX2;
    private static final int MAX_SANE_PUBLIC_KEY_LENGTH = 1024 * 32;
    private static final int MAX_SANE_SIGNATURE_LENGTH = 1024 * 128;

    /**
     * Constructs a new instance.
     */
    public Crx2Packer() {
    }

    static CrxPacker getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    @Override
    public void packExtension(ByteSource zipBytes, KeyPair keyPair, OutputStream output) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        writeExtensionHeader(zipBytes, keyPair, output);
        zipBytes.copyTo(output);
    }

    protected void writeExtensionHeader(ByteSource zipBytes, KeyPair keyPair, OutputStream output) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        byte[] signature = sign(zipBytes, keyPair);
        writeExtensionHeader(publicKeyBytes, signature, output);
    }

    protected void writeExtensionHeader(byte[] publicKeyBytes, byte[] signature, OutputStream output) throws IOException {
        checkArgument(publicKeyBytes.length <= MAX_SANE_PUBLIC_KEY_LENGTH, "public key length is insane: %s", publicKeyBytes.length);
        checkArgument(signature.length <= MAX_SANE_SIGNATURE_LENGTH, "signature length is insane: %s", signature.length);
        LittleEndianDataOutputStream leOutput = new LittleEndianDataOutputStream(output);
        CrxPackers.writeMagicNumber(leOutput, MAGIC_NUMBER);
        CrxPackers.writeFormatVersion(leOutput, getCrxVersion());
        leOutput.writeInt(publicKeyBytes.length);
        leOutput.writeInt(signature.length);
        leOutput.flush();
        output.write(publicKeyBytes);
        output.write(signature);
    }

    private static final String HASH_FUNCTION = "SHA1";
    private static final String CRYPTO_ALGORITHM = "RSA";

    protected byte[] sign(ByteSource zipBytes, KeyPair keyPair) throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        return createSigner().sign(zipBytes.read(), keyPair.getPrivate());
    }

    protected Signer createSigner() {
        return new BasicSigner(HASH_FUNCTION, CRYPTO_ALGORITHM);
    }

    @Override
    public CrxVersion getCrxVersion() {
        return CRX_VERSION;
    }


}

