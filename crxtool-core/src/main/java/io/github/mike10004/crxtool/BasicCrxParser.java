package io.github.mike10004.crxtool;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.primitives.Ints;
import com.google.common.primitives.UnsignedInteger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Basic implementation of a Chrome extension parser.
 */
public class BasicCrxParser implements CrxParser {

    private static final BasicCrxParser DEFAULT_INSTANCE = new BasicCrxParser();

    /**
     * Constructs an instance.
     */
    public BasicCrxParser() {}

    private void checkMagicNumber(String magicNumber) throws io.github.mike10004.crxtool.CrxParsingException {
        if (!"Cr24".equals(magicNumber)) {
            try {
                byte[] magicNumberBytes = magicNumber.getBytes(StandardCharsets.US_ASCII);
                throw new io.github.mike10004.crxtool.CrxParsingException("incorrect magic number: 0x" + BaseEncoding.base16().encode(magicNumberBytes));
            } catch (RuntimeException e) {
                throw new io.github.mike10004.crxtool.CrxParsingException("incorrect magic number (unreportable)");
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
        CrxMetadata metadata = interpreter.parseMetadataAfterVersion(crxInput);
        return metadata;
    }

    protected CrxInterpreter getCrxInterpreter(@SuppressWarnings("unused") String magicNumber, int version) throws CrxInterpreter.UnsupportedCrxVersionException {
        switch (version) {
            case 2:
                return new Crx2Interpreter(magicNumber, version);
            case 3:
                return new Crx3Interpreter(magicNumber, version);
            default:
                throw new CrxInterpreter.UnsupportedCrxVersionException("version " + version + " is not supported");
        }
    }

    protected interface CrxInterpreter {

        CrxMetadata parseMetadataAfterVersion(InputStream crxInput) throws IOException;

        class UnsupportedCrxVersionException extends io.github.mike10004.crxtool.CrxParsingException {

            public UnsupportedCrxVersionException(String message) {
                super(message);
            }
        }
    }

    static BasicCrxParser getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }
}
