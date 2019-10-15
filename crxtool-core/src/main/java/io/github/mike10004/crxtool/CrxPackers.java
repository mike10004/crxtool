package io.github.mike10004.crxtool;

import com.google.common.io.ByteSource;
import com.google.common.io.LittleEndianDataOutputStream;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

class CrxPackers {

    public static final String MAGIC_NUMBER = "Cr24";

    private CrxPackers() {}

    public static byte[] sign(ByteSource zipBytes, KeyPair keyPair, Signature sig) throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        return sign(zipBytes, keyPair, sig, null);
    }

    public static byte[] sign(ByteSource zipBytes, KeyPair keyPair, Signature sig, @Nullable SecureRandom random) throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        if (random == null) {
            sig.initSign(keyPair.getPrivate());
        } else {
            sig.initSign(keyPair.getPrivate(), random);
        }
        sig.update(zipBytes.read());
        byte[] signatureBytes = sig.sign();
        return signatureBytes;
    }

    public static void writeMagicNumber(LittleEndianDataOutputStream leOutput, String magicNumber) throws IOException {
        byte[] magicBytes = magicNumber.getBytes(StandardCharsets.US_ASCII);
        leOutput.write(magicBytes);
    }

    public static void writeFormatVersion(LittleEndianDataOutputStream leOutput, CrxVersion version) throws IOException {
        leOutput.writeInt(version.identifier());
    }


}
