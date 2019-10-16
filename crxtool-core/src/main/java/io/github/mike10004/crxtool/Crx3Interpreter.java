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

    Crx3Interpreter(String magicNumber) {
        super(magicNumber, CrxVersion.CRX3);
    }

    @Override
    public CrxMetadata parseMetadataAfterVersion(InputStream crxInput, ParsingState state) throws IOException {
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(crxInput);
        int headerLen;
        try (SegmentMark ignore = state.markStart("fileHeaderLength")) {
            headerLen = Ints.checkedCast(UnsignedInteger.fromIntBits(in.readInt()).longValue());
        }
        if (headerLen <= 0 || headerLen > MAX_SANE_HEADER_LEN) {
            throw new CrxParsingException(String.format("reported header length is insane: %s", headerLen));
        }
        byte[] headerBytes = new byte[headerLen];
        try (SegmentMark ignore = state.markStart("fileHeader")) {
            ByteStreams.readFully(crxInput, headerBytes);
        }
        return parseFileHeader(headerBytes);
    }

    private CrxMetadata parseFileHeader(byte[] headerBytes) throws IOException {
        Crx3.CrxFileHeader parsedHeader = Crx3.CrxFileHeader.parseFrom(headerBytes);
        CrxFileHeader fileHeader = new MessageFileHeader(parsedHeader);
        AsymmetricKeyProof proof = fileHeader.getAllAsymmetricKeyProofs().stream()
                .findFirst().orElseThrow(() -> new CrxParsingException("header does not contain any asymmetric key proofs"))
                .proof();
        HashCode pubkeyHash = hashBase64(SHA256, proof.getPublicKeyBase64());
        String digest = pubkeyHash.toString().toLowerCase(Locale.ROOT);
        StringBuilder idBuilder = new StringBuilder(ID_LEN);
        translateDigestToId(digest, 0, ID_LEN, idBuilder);
        String id = idBuilder.toString();
        return new BufferedCrxMetadata(magicNumber, version, fileHeader, id);
    }

    @SuppressWarnings("SameParameterValue")
    protected HashCode hashBase64(HashFunction hashFunction, String base64Data) throws IOException {
        return BASE_64.decodingSource(CharSource.wrap(base64Data)).hash(hashFunction);
    }

}
