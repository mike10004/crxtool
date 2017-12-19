package io.github.mike10004.crxtool;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedInteger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkState;

/**
 * Basic implementation of a Chrome extension parser.
 */
public class BasicCrxParser implements CrxParser {

    private static final int MAX_SANE_PUBKEY_LENGTH = 1024 * 32;
    private static final int MAX_SANE_SIGNATURE_LENGTH = 1024 * 64;

    private static final BasicCrxParser DEFAULT_INSTANCE = new BasicCrxParser();
    private static final int ID_LEN = 32;
    private static final char[] DIGEST_CHARS = "0123456789abcdef".toCharArray();
    private static final char[] CRX_ID_CHARS = "abcdefghijklmnop".toCharArray();

    /**
     * Constructs an instance.
     */
    public BasicCrxParser() {}

    private void checkMagicNumber(String magicNumber) throws CrxParsingException {
        if (!"Cr24".equals(magicNumber)) {
            throw new CrxParsingException("incorrect magic number: " + magicNumber);
        }
    }

    @Override
    public CrxMetadata parseMetadata(InputStream crxInput) throws IOException {
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(crxInput);
        byte[] magicNumberBytes = new byte[4];
        in.readFully(magicNumberBytes);
        String magicNumber = new String(magicNumberBytes, StandardCharsets.US_ASCII);
        checkMagicNumber(magicNumber);
        int version = Ints.checkedCast(UnsignedInteger.fromIntBits(in.readInt()).longValue());
        int pubkeyLength = Ints.checkedCast(UnsignedInteger.fromIntBits(in.readInt()).longValue());
        int signatureLength = Ints.checkedCast(UnsignedInteger.fromIntBits(in.readInt()).longValue());
        if (pubkeyLength <= 0 || pubkeyLength > MAX_SANE_PUBKEY_LENGTH) {
            throw new CrxParsingException(String.format("public key length is insane: %s", pubkeyLength));
        }
        if (signatureLength <= 0 || signatureLength > MAX_SANE_SIGNATURE_LENGTH) {
            throw new CrxParsingException(String.format("signature length is insane: %s", signatureLength));
        }
        byte[] pubkeyBytes = new byte[pubkeyLength];
        ByteStreams.readFully(crxInput, pubkeyBytes);
        byte[] signatureBytes = new byte[signatureLength];
        ByteStreams.readFully(crxInput, signatureBytes);
        BaseEncoding encoding = BaseEncoding.base64();
        String pubkeyBase64 = encoding.encode(pubkeyBytes);
        String signatureBase64 = encoding.encode(signatureBytes);
        HashCode pubkeyHash = Hashing.sha256().hashBytes(pubkeyBytes);
        String digest = pubkeyHash.toString().toLowerCase(Locale.ROOT);
        StringBuilder idBuilder = new StringBuilder(ID_LEN);
        translate(DIGEST_CHARS, CRX_ID_CHARS, digest, 0, ID_LEN, idBuilder);
        String id = idBuilder.toString();
        return new CrxMetadata(magicNumber, version, pubkeyLength, pubkeyBase64, signatureLength, signatureBase64, id);
    }

    @SuppressWarnings("SameParameterValue")
    private static void translate(char[] from, char[] to, String source, int sourceStart, int sourceLen, StringBuilder sink) {
        if (from.length != to.length) {
            throw new IllegalArgumentException("arrays must be congruent");
        }
        for (int i = sourceStart; i < (sourceStart + sourceLen); i++) {
            char untranslated = source.charAt(i);
            int fromIndex = Arrays.binarySearch(from, untranslated);
            char translated = untranslated;
            if (fromIndex >= 0) {
                translated = to[fromIndex];
            }
            sink.append(translated);
        }
    }

    static BasicCrxParser getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }
}
