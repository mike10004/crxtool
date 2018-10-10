package io.github.mike10004.crxtool;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSource;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedInteger;
import io.github.mike10004.crxtool.message.Crx3;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * https://cs.chromium.org/chromium/src/components/crx_file/crx3.proto
 */
class Crx3Interpreter extends CrxInterpreterBase {

    private static final int MAX_SANE_HEADER_LEN = 1024 * 128;

    Crx3Interpreter(String magicNumber, int version) {
        super(magicNumber, version);
    }

    @Override
    public CrxMetadata parseMetadataAfterVersion(InputStream crxInput) throws IOException {
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(crxInput);
        int headerLen = Ints.checkedCast(UnsignedInteger.fromIntBits(in.readInt()).longValue());
        if (headerLen <= 0 || headerLen > MAX_SANE_HEADER_LEN) {
            throw new CrxParser.CrxParsingException(String.format("reported header length is insane: %s", headerLen));
        }
        byte[] headerBytes = new byte[headerLen];
        ByteStreams.readFully(crxInput, headerBytes);
        System.out.format("header:%n%n%s%n%n", BASE_64.encode(headerBytes));
        Crx3.CrxFileHeader parsedHeader = Crx3.CrxFileHeader.parseFrom(headerBytes);
        CrxFileHeader fileHeader = new MessageFileHeader(parsedHeader);
        AsymmetricKeyProof rsaProof = fileHeader.getAsymmetricKeyProofs(KeyPairs.ALGORITHM_SHA256_WITH_RSA).stream().findFirst().orElse(null);
        if (rsaProof == null) {
            throw new CrxParser.CrxParsingException("header does not contain sha256_with_rsa asymmetric key proof");
        }
        HashCode pubkeyHash = hashBase64(SHA256, rsaProof.getPublicKeyBase64());
        String digest = pubkeyHash.toString().toLowerCase(Locale.ROOT);
        StringBuilder idBuilder = new StringBuilder(ID_LEN);
        translateDigestToId(digest, 0, ID_LEN, idBuilder);
        String id = idBuilder.toString();
        return new CrxMetadata(magicNumber, version, fileHeader, id);
    }

    protected HashCode hashBase64(HashFunction hashFunction, String base64Data) throws IOException {
        return BASE_64.decodingSource(CharSource.wrap(base64Data)).hash(hashFunction);
    }

}
