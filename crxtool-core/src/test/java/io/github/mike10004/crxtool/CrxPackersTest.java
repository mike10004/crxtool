package io.github.mike10004.crxtool;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.google.common.io.CountingOutputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import com.google.common.primitives.Ints;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class CrxPackersTest {

    @Test
    public void writeFormatVersion_crx3() throws Exception {
        writeFormatVersion(new Crx3Packer(), "03000000");
    }

    @SuppressWarnings("SameParameterValue")
    private void writeFormatVersion(CrxPacker packer, String expectedFormatVersionHex) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(32);
        CountingOutputStream counter = new CountingOutputStream(buffer);
        LittleEndianDataOutputStream output = new LittleEndianDataOutputStream(counter);
        CrxPackers.writeFormatVersion(output, packer.getCrxVersion());
        output.flush();
        long count = counter.getCount();
        assertEquals("byte count", 4, count);
        byte[] expected = BaseEncoding.base16().decode(expectedFormatVersionHex);
        byte[] actual = buffer.toByteArray();
        assertArrayEquals("version encoding", expected, actual);
    }

}