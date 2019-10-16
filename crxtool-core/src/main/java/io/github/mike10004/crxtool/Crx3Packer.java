package io.github.mike10004.crxtool;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.LittleEndianDataOutputStream;
import com.google.common.primitives.Ints;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedOutputStream;
import io.github.mike10004.crxtool.message.Crx3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

public class Crx3Packer implements CrxPacker {

    private static final String MAGIC_NUMBER = CrxPackers.MAGIC_NUMBER;

    @Override
    public void packExtension(ByteSource zipBytes, KeyPair keyPair, OutputStream output) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        byte[] crxId = deriveCrxId(keyPair);
        Crx3.SignedData signedData = Crx3.SignedData.newBuilder()
                .setCrxId(ByteString.copyFrom(crxId))
                .build();
        byte[] signedHeaderData = signedData.toByteArray();
        byte[] signature = sign(zipBytes, signedHeaderData, keyPair);
        Crx3.AsymmetricKeyProof proof = Crx3.AsymmetricKeyProof.newBuilder()
                .setPublicKey(getPublicKeyByteString(keyPair))
                .setSignature(ByteString.copyFrom(signature))
                .build();
        Crx3.CrxFileHeader fileHeader = Crx3.CrxFileHeader.newBuilder()
                .setSignedHeaderData(signedData.toByteString())
                .addSha256WithRsa(proof)
//                .setSha256WithRsa(0, proof)
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

    protected byte[] deriveCrxId(KeyPair keyPair) {
        // https://github.com/gromnitsky/crx3-utils/blob/master/index.js
        // crypto.createHash('sha256').update(public_key).digest().slice(0, 16)
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        byte[] hash = idHashFunction().hashBytes(publicKeyBytes).asBytes();
        byte[] id = new byte[CRX_ID_LEN];
        System.arraycopy(hash, 0, id, 0, Math.min(id.length, hash.length));
        return id;
    }

    protected HashFunction idHashFunction() {
        return Hashing.sha256();
    }

    private static final int CRX_ID_LEN = 16;
    private static final String HASH_FUNCTION = "SHA256";
    private static final String CRYPTO_ALGORITHM = "RSA";

    /**
     * All proofs are on the value:
     * "CRX3 SignedData\x00" + signed_header_size + signed_header_data + archive
     * @param zipBytes
     * @param signedHeaderData
     * @param keyPair
     * @return
     * @throws IOException
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    protected byte[] sign(ByteSource zipBytes, byte[] signedHeaderData, KeyPair keyPair) throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] payload = zipBytes.read();
        int payloadLen = payload.length;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(payload.length + 1024); // TODO compute actual expected size
        buffer.write("CRX3 SignedData".getBytes(StandardCharsets.UTF_8));
        buffer.write(new byte[]{0});
        CrxPackers.writeLittleEndian(signedHeaderData.length, buffer);
        // write PB tag?
        buffer.write(signedHeaderData);
        buffer.write(payload);
        byte[] data = buffer.toByteArray();
        return createSigner().sign(data, keyPair.getPrivate());
    }

    protected Signer createSigner() {
        return new BasicSigner(HASH_FUNCTION, CRYPTO_ALGORITHM);
    }

    protected ByteString getPublicKeyByteString(KeyPair keyPair) {
        return ByteString.copyFrom(keyPair.getPublic().getEncoded());
    }

    @Override
    public CrxVersion getCrxVersion() {
        return CrxVersion.CRX3;
    }
}
