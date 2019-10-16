package io.github.mike10004.crxtool;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.LittleEndianDataOutputStream;
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
    public void packExtension(InputSource zipBytes, KeyPair keyPair, OutputStream output) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        byte[] crxId = deriveCrxId(keyPair);
        Crx3.SignedData signedData = Crx3.SignedData.newBuilder()
                .setCrxId(ByteString.copyFrom(crxId))
                .build();
        byte[] signature = sign(zipBytes, signedData, keyPair);
        Crx3.AsymmetricKeyProof proof = Crx3.AsymmetricKeyProof.newBuilder()
                .setPublicKey(getPublicKeyByteString(keyPair))
                .setSignature(ByteString.copyFrom(signature))
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

    /**
     * See {@code crx3.proto}.
     */
    protected byte[] deriveCrxId(KeyPair keyPair) {
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        byte[] hash = idHashFunction().hashBytes(publicKeyBytes).asBytes();
        byte[] id = new byte[CRX_ID_LEN];
        System.arraycopy(hash, 0, id, 0, Math.min(id.length, hash.length));
        return id;
    }

    private static HashFunction idHashFunction() {
        return Hashing.sha256();
    }

    private static final int CRX_ID_LEN = 16;
    private static final String HASH_FUNCTION = "SHA256";
    private static final String CRYPTO_ALGORITHM = "RSA";

    /**
     * From crx3.proto documentation:
     * <pre>
     * All proofs are on the value:
     * "CRX3 SignedData\x00" + signed_header_size + signed_header_data + archive
     * </pre>
     * @param zipBytes zip archive byte source
     * @param signedHeaderData signed header data
     * @param keyPair the key pair
     * @return the signature
     * @throws IOException on I/O error
     * @throws SignatureException on signature error
     * @throws InvalidKeyException on invalid key
     * @throws NoSuchAlgorithmException if algorithm spec is not valid
     */
    protected byte[] sign(InputSource zipBytes, Crx3.SignedData signedHeaderData, KeyPair keyPair) throws IOException, SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        byte[] payload = zipBytes.read();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(payload.length + 1024); // TODO compute actual expected size
        buffer.write("CRX3 SignedData".getBytes(StandardCharsets.UTF_8));
        buffer.write(new byte[]{0});
        byte[] signedHeaderDataBytes = signedHeaderData.toByteArray();
        CrxPackers.writeLittleEndian(signedHeaderDataBytes.length, buffer);
        buffer.write(signedHeaderDataBytes);
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
