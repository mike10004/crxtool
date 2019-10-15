package io.github.mike10004.crxtool;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.LittleEndianDataOutputStream;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import io.github.mike10004.crxtool.message.Crx3;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

public class Crx3Packer implements CrxPacker {

    private static final String MAGIC_NUMBER = CrxPackers.MAGIC_NUMBER;

    @Override
    public void packExtension(ByteSource zipBytes, KeyPair keyPair, OutputStream output) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Crx3.AsymmetricKeyProof proof = Crx3.AsymmetricKeyProof.newBuilder()
                .setPublicKey(getPublicKeyByteString(keyPair))
                .setSignature(sign(zipBytes, keyPair))
                .build();
        Crx3.SignedData signedData = Crx3.SignedData.newBuilder()
                .setCrxId(getCrxId(keyPair))
                .build();
        Crx3.CrxFileHeader fileHeader = Crx3.CrxFileHeader.newBuilder()
                .setSignedHeaderData(signedData.toByteString())
                .addSha256WithRsa(proof)
                .build();
        LittleEndianDataOutputStream leOutput = new LittleEndianDataOutputStream(output);
        CrxPackers.writeMagicNumber(leOutput, MAGIC_NUMBER);
        CrxPackers.writeFormatVersion(leOutput, getCrxVersion());
        int fileHeaderSize = fileHeader.getSerializedSize();
        leOutput.writeInt(fileHeaderSize);
        leOutput.flush();
        CodedOutputStream codedOutput = CodedOutputStream.newInstance(output);
        fileHeader.writeTo(codedOutput);
        codedOutput.flush();
        zipBytes.copyTo(output);
        output.flush();
    }

    protected ByteString getCrxId(KeyPair keyPair) {
        // https://github.com/gromnitsky/crx3-utils/blob/master/index.js
        // crypto.createHash('sha256').update(public_key).digest().slice(0, 16)
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        byte[] hash = Hashing.sha256().hashBytes(publicKeyBytes).asBytes();
        return ByteString.copyFrom(hash);
    }

    protected ByteString sign(ByteSource zipBytes, KeyPair keyPair) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        byte[] signage = CrxPackers.sign(zipBytes, keyPair, Signature.getInstance("SHA256WithRSA"));
        return ByteString.copyFrom(signage);
    }

    protected ByteString getPublicKeyByteString(KeyPair keyPair) {
        return ByteString.copyFrom(keyPair.getPublic().getEncoded());
    }

    @Override
    public CrxVersion getCrxVersion() {
        return CrxVersion.CRX3;
    }
}
