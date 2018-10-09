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

/**
 * Basic implementation of a Chrome extension parser.
 */
public class BasicCrxParser implements CrxParser {

    private static final BasicCrxParser DEFAULT_INSTANCE = new BasicCrxParser();

    /**
     * Constructs an instance.
     */
    public BasicCrxParser() {}

    private void checkMagicNumber(String magicNumber) throws CrxParsingException {
        if (!"Cr24".equals(magicNumber)) {
            try {
                byte[] magicNumberBytes = magicNumber.getBytes(StandardCharsets.US_ASCII);
                throw new CrxParsingException("incorrect magic number: 0x" + BaseEncoding.base16().encode(magicNumberBytes));
            } catch (RuntimeException e) {
                throw new CrxParsingException("incorrect magic number (unreportable)");
            }

        }
    }

    private static final int EXPECTED_MAGIC_NUMBER_LEN_BYTES = 4;

    protected String readMagicNumber(InputStream in) throws IOException {
        byte[] magicNumberBytes = new byte[EXPECTED_MAGIC_NUMBER_LEN_BYTES];
        ByteStreams.readFully(in, magicNumberBytes);
        String magicNumber = new String(magicNumberBytes, StandardCharsets.US_ASCII);
        return magicNumber;
    }

    @Override
    public CrxMetadata parseMetadata(InputStream crxInput) throws IOException {
        String magicNumber = readMagicNumber(crxInput);
        checkMagicNumber(magicNumber);
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(crxInput);
        int version = Ints.checkedCast(UnsignedInteger.fromIntBits(in.readInt()).longValue());
        CrxInterpreter interpreter = getCrxInterpreter(magicNumber, version);
        PostVersionMetadata postVersionMetadata = interpreter.parseMetadataAfterVersion(crxInput, in);
        CrxMetadata metadata = new CrxMetadata(magicNumber, version, postVersionMetadata.pubkeyLength, postVersionMetadata.pubkeyBase64, postVersionMetadata.signatureLength, postVersionMetadata.signatureBase64, postVersionMetadata.id);
        return metadata;
    }

    protected CrxInterpreter getCrxInterpreter(@SuppressWarnings("unused") String magicNumber, int version) throws CrxInterpreter.UnsupportedCrxVersionException {
        switch (version) {
            case 2:
                return new Crx2Interpreter();
            case 3:
                return new Crx3Interpreter();
            default:
                throw new CrxInterpreter.UnsupportedCrxVersionException("version " + version + " is not supported");
        }
    }

    protected interface CrxInterpreter {

        PostVersionMetadata parseMetadataAfterVersion(InputStream crxInput, LittleEndianDataInputStream in) throws IOException;

        class UnsupportedCrxVersionException extends CrxParsingException {

            public UnsupportedCrxVersionException(String message) {
                super(message);
            }
        }
    }

    protected void a() {
    }

    static BasicCrxParser getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }
}
