package io.github.mike10004.crxtool;

import com.google.common.io.LittleEndianDataOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class CrxPackers {

    public static final String MAGIC_NUMBER = "Cr24";

    private CrxPackers() {}

    public static void writeMagicNumber(LittleEndianDataOutputStream leOutput, String magicNumber) throws IOException {
        byte[] magicBytes = magicNumber.getBytes(StandardCharsets.US_ASCII);
        leOutput.write(magicBytes);
    }

    public static void writeFormatVersion(LittleEndianDataOutputStream leOutput, CrxVersion version) throws IOException {
        leOutput.writeInt(version.identifier());
    }

    public static void writeLittleEndian(int value, OutputStream output) throws IOException {
        LittleEndianDataOutputStream leOutput;
        if (output instanceof LittleEndianDataOutputStream) {
            leOutput = (LittleEndianDataOutputStream) output;
        } else {
            leOutput = new LittleEndianDataOutputStream(output);
        }
        leOutput.writeInt(value);
        leOutput.flush();
    }
}
