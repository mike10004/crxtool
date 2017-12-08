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

public class BasicCrxPacker implements CrxPacker {

    private static final String MAGIC_NUMBER = "Cr24";
    private static final int FORMAT_VERSION = 2;

    private static final BasicCrxPacker DEFAULT_INSTANCE = new BasicCrxPacker();

    public BasicCrxPacker() {
    }

    @Override
    public void packExtension(ByteSource zipBytes, KeyPair keyPair, OutputStream output) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        LittleEndianDataOutputStream leOutput = new LittleEndianDataOutputStream(output);
        writeMagicNumber(leOutput);
        writeFormatVersion(leOutput);
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        leOutput.writeInt(publicKeyBytes.length);
        byte[] signature = sign(zipBytes, keyPair);
        leOutput.writeInt(signature.length);
        leOutput.flush();
        output.write(publicKeyBytes);
        output.write(signature);
        zipBytes.copyTo(output);
    }

    protected void writeMagicNumber(LittleEndianDataOutputStream leOutput) throws IOException {
        leOutput.write(MAGIC_NUMBER.getBytes(StandardCharsets.US_ASCII));
    }

    protected void writeFormatVersion(LittleEndianDataOutputStream leOutput) throws IOException {
        leOutput.writeInt(FORMAT_VERSION);
    }

    protected byte[] sign(ByteSource zipBytes, KeyPair keyPair) throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initSign(keyPair.getPrivate());
        sig.update(zipBytes.read());
        byte[] signatureBytes = sig.sign();
        return signatureBytes;
    }

    static CrxPacker getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }
}
