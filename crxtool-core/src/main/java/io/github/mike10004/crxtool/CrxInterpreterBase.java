package io.github.mike10004.crxtool;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import java.util.Arrays;

abstract class CrxInterpreterBase implements BasicCrxParser.CrxInterpreter {

    protected static final int ID_LEN = 32;
    protected static final char[] DIGEST_CHARS = "0123456789abcdef".toCharArray();
    protected static final char[] CRX_ID_CHARS = "abcdefghijklmnop".toCharArray();

    protected final String magicNumber;
    protected final int version;

    protected CrxInterpreterBase(String magicNumber, int version) {
        this.magicNumber = magicNumber;
        this.version = version;
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

    @SuppressWarnings("SameParameterValue")
    protected static void translateDigestToId(String source, int sourceStart, int sourceLen, StringBuilder sink) {
        translate(DIGEST_CHARS, CRX_ID_CHARS, source, sourceStart, sourceLen, sink);
    }

    protected static final BaseEncoding BASE_64 = BaseEncoding.base64();

    protected static final HashFunction SHA256 = Hashing.sha256();

}

