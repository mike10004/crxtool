package com.github.mike10004.crxtool.maven;

import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteSource;
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

public class PemParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void extractBytes() throws Exception {
        File gzippedPemFile = new File(getClass().getResource("/key-for-testing.rsa.gz").toURI());
        File pemFile = temporaryFolder.newFile("key.pem");
        try (InputStream in = new GZIPInputStream(new FileInputStream(gzippedPemFile))) {
            Files.asByteSink(pemFile).writeFrom(in);
        }
        PemParser parser = new PemParser();
        byte[] bytes;
        try (Reader reader = new InputStreamReader(new FileInputStream(pemFile), StandardCharsets.US_ASCII)) {
            bytes = parser.extractBytes(reader);
        }
        assertEquals("num bytes", 1191, bytes.length);
        int numZeros = ByteSource.wrap(bytes).read(new ZeroCounter());
        assertNotEquals("num zeroes", bytes.length, numZeros);
    }

    private static class ZeroCounter implements ByteProcessor<Integer> {
        private int zeroCount = 0;

        @SuppressWarnings("NullableProblems")
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