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
import java.util.Arrays;
import java.util.Locale;

abstract class CrxInterpreterBase implements BasicCrxParser.CrxInterpreter {

    protected static final int MAX_SANE_PUBKEY_LENGTH = 1024 * 32;
    protected static final int MAX_SANE_SIGNATURE_LENGTH = 1024 * 64;

    protected static final int ID_LEN = 32;
    protected static final char[] DIGEST_CHARS = "0123456789abcdef".toCharArray();
    protected static final char[] CRX_ID_CHARS = "abcdefghijklmnop".toCharArray();

    @SuppressWarnings("SameParameterValue")
    protected static void translate(char[] from, char[] to, String source, int sourceStart, int sourceLen, StringBuilder sink) {
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

}

class Crx2Interpreter extends CrxInterpreterBase {

    @Override
    public PostVersionMetadata parseMetadataAfterVersion(InputStream crxInput, LittleEndianDataInputStream in) throws IOException {
        int pubkeyLength = Ints.checkedCast(UnsignedInteger.fromIntBits(in.readInt()).longValue());
        int signatureLength = Ints.checkedCast(UnsignedInteger.fromIntBits(in.readInt()).longValue());
        if (pubkeyLength <= 0 || pubkeyLength > MAX_SANE_PUBKEY_LENGTH) {
            throw new CrxParser.CrxParsingException(String.format("public key length is insane: %s", pubkeyLength));
        }
        if (signatureLength <= 0 || signatureLength > MAX_SANE_SIGNATURE_LENGTH) {
            throw new CrxParser.CrxParsingException(String.format("signature length is insane: %s", signatureLength));
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
        return new PostVersionMetadata(pubkeyLength, pubkeyBase64, signatureLength, signatureBase64, id);

    }

}

class Crx3Interpreter extends CrxInterpreterBase {

    @Override
    public PostVersionMetadata parseMetadataAfterVersion(InputStream crxInput, LittleEndianDataInputStream in) throws IOException {
        throw new UnsupportedCrxVersionException("version 3 support not yet implemented");
    }

}
