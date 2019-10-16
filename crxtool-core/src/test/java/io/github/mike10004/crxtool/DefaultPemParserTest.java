package io.github.mike10004.crxtool;

import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class DefaultPemParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void extractBytes() throws Exception {
        CharSource pemSource = TestingKey.getTestingKeyPemSource();
        PemParser parser = new DefaultPemParser();
        byte[] bytes;
        try (Reader reader = pemSource.openStream()) {
            bytes = parser.extractBytes(reader);
        }
        assertEquals("num bytes", 1217, bytes.length);
        int numZeros = ByteSource.wrap(bytes).read(new ZeroCounter());
        assertNotEquals("num zeroes", bytes.length, numZeros);
    }

    private static class ZeroCounter implements ByteProcessor<Integer> {
        private int zeroCount = 0;

        @Override
        public boolean processBytes(byte[] buf, int off, int len) {
            for (byte b : buf) {
                if (b == 0) {
                    zeroCount++;
                }
            }
            return true;
        }

        @Override
        public Integer getResult() {
            return zeroCount;
        }

    }
}