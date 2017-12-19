package io.github.mike10004.crxtool.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UnzippageTest {

    private static final Set<String> requiredReferenceDirs = ImmutableSet.of("ziptest/d1/d4/");
    private static final Map<String, byte[]> requiredReferenceFiles = ImmutableMap.of(
            "ziptest/d1/d2/d3/a", "a\n".getBytes(US_ASCII),
            "ziptest/d1/d5/b", "b\n".getBytes(US_ASCII),
            "ziptest/d1/d", "d\n".getBytes(US_ASCII),
            "ziptest/d1/c", "c\n".getBytes(US_ASCII)
    );

    @Test
    public void unzip_file_clean() throws Exception {
        File zipFile = File.createTempFile("reference", ".zip");
        Files.write(zipFile.toPath(), getReferenceZipBytes());
        Unzippage unzippage = Unzippage.unzip(zipFile);
        check(unzippage);
    }

    @Test
    public void unzip_stream_clean() throws Exception {
        byte[] bytes = getReferenceZipBytes();
        Unzippage unzippage;
        try (InputStream stream = new ByteArrayInputStream(bytes)) {
            unzippage = Unzippage.unzip(stream);
        }
        check(unzippage);
    }

    @Test(expected = java.util.zip.ZipException.class)
    public void unzip_file_notZipData() throws Exception {
        byte[] bytes = new byte[10 * 1024];
        Random random = new Random(getClass().getName().hashCode());
        random.nextBytes(bytes);
        File zipFile = File.createTempFile("notazipfile", ".zip");
        java.nio.file.Files.write(zipFile.toPath(), bytes);
        Unzippage.unzip(zipFile);
    }

    @Test(expected = java.util.zip.ZipException.class)
    public void unzip_stream_notZipData() throws Exception {
        byte[] bytes = new byte[10 * 1024];
        Random random = new Random(getClass().getName().hashCode());
        random.nextBytes(bytes);
        Unzippage.unzip(new ByteArrayInputStream(bytes));
    }

    @Test(expected = java.util.zip.ZipException.class)
    public void unzip_stream_crx() throws Exception {
        File zipFile = new File(getClass().getResource("/extension.crx").toURI());
        try (InputStream in = new FileInputStream(zipFile)) {
            Unzippage.unzip(in);
        }
    }

    @Test(expected = java.util.zip.ZipException.class)
    public void unzip_file_crx() throws Exception {
        File zipFile = new File(getClass().getResource("/extension.crx").toURI());
        Unzippage.unzip(zipFile);
    }

    @Test
    public void unzip_file_crx_noIntegrity() throws Exception {
        File zipFile = new File(getClass().getResource("/extension.crx").toURI());
        Unzippage unzippage = Unzippage.unzip(zipFile, UnzipConfig.getDefault(), ZipIntegrityConstraint.IGNORE_ERRORS);
        assertEquals("file entries", ImmutableSet.of("manifest.json", "background.js"), ImmutableSet.copyOf(unzippage.fileEntries()));
    }

    private void check(Unzippage unzippage) throws IOException {
        System.out.format("directories: %s%n", ImmutableList.copyOf(unzippage.directoryEntries()));
        System.out.format("files: %s%n", ImmutableList.copyOf(unzippage.fileEntries()));
        assertTrue("expected empty reference dir", ImmutableSet.copyOf(unzippage.directoryEntries()).containsAll(requiredReferenceDirs));
        Map<String, byte[]> arrayValues = new HashMap<>();
        for (String entryName : unzippage.fileEntries()) {
            @Nullable ByteSource byteSource = unzippage.getFileBytes(entryName);
            assertNotNull("byte source for entry expected to be non-null", byteSource);
            arrayValues.put(entryName, byteSource.read());
        }
        assertEquals("files", requiredReferenceFiles.keySet(), arrayValues.keySet());
        requiredReferenceFiles.forEach((entryName, bytes) -> {
            assertArrayEquals(entryName, bytes, arrayValues.get(entryName));
        });
    }

    private static byte[] getReferenceZipBytes() {
        return Base64.getDecoder().decode(REFERENCE_ZIP_BASE64);
    }

    public static final String REFERENCE_ZIP_BASE64 =
            "UEsDBAoAAAAAADhtdUsAAAAAAAAAAAAAAAAIABwAemlwdGVzdC9VVAkAA2tzFFq8cxRadXgLAAEE" +
                    "6AMAAAToAwAAUEsDBAoAAAAAAFVtdUsAAAAAAAAAAAAAAAALABwAemlwdGVzdC9kMS9VVAkAA6Jz" +
                    "FFq8cxRadXgLAAEE6AMAAAToAwAAUEsDBAoAAAAAADptdUsAAAAAAAAAAAAAAAAOABwAemlwdGVz" +
                    "dC9kMS9kNC9VVAkAA29zFFq8cxRadXgLAAEE6AMAAAToAwAAUEsDBAoAAAAAADhtdUsAAAAAAAAA" +
                    "AAAAAAAOABwAemlwdGVzdC9kMS9kMi9VVAkAA2tzFFq8cxRadXgLAAEE6AMAAAToAwAAUEsDBAoA" +
                    "AAAAAEdtdUsAAAAAAAAAAAAAAAARABwAemlwdGVzdC9kMS9kMi9kMy9VVAkAA4VzFFq8cxRadXgL" +
                    "AAEE6AMAAAToAwAAUEsDBAoAAAAAAEdtdUsHoerdAgAAAAIAAAASABwAemlwdGVzdC9kMS9kMi9k" +
                    "My9hVVQJAAOFcxRahXMUWnV4CwABBOgDAAAE6AMAAGEKUEsDBAoAAAAAAFBtdUsAAAAAAAAAAAAA" +
                    "AAAOABwAemlwdGVzdC9kMS9kNS9VVAkAA5hzFFq8cxRadXgLAAEE6AMAAAToAwAAUEsDBAoAAAAA" +
                    "AFBtdUvE8sf2AgAAAAIAAAAPABwAemlwdGVzdC9kMS9kNS9iVVQJAAOYcxRamHMUWnV4CwABBOgD" +
                    "AAAE6AMAAGIKUEsDBAoAAAAAAFVtdUtCVZ2gAgAAAAIAAAAMABwAemlwdGVzdC9kMS9kVVQJAAOi" +
                    "cxRaonMUWnV4CwABBOgDAAAE6AMAAGQKUEsDBAoAAAAAAFNtdUuFw9zvAgAAAAIAAAAMABwAemlw" +
                    "dGVzdC9kMS9jVVQJAAOdcxRanXMUWnV4CwABBOgDAAAE6AMAAGMKUEsBAh4DCgAAAAAAOG11SwAA" +
                    "AAAAAAAAAAAAAAgAGAAAAAAAAAAQAO1BAAAAAHppcHRlc3QvVVQFAANrcxRadXgLAAEE6AMAAATo" +
                    "AwAAUEsBAh4DCgAAAAAAVW11SwAAAAAAAAAAAAAAAAsAGAAAAAAAAAAQAO1BQgAAAHppcHRlc3Qv" +
                    "ZDEvVVQFAAOicxRadXgLAAEE6AMAAAToAwAAUEsBAh4DCgAAAAAAOm11SwAAAAAAAAAAAAAAAA4A" +
                    "GAAAAAAAAAAQAO1BhwAAAHppcHRlc3QvZDEvZDQvVVQFAANvcxRadXgLAAEE6AMAAAToAwAAUEsB" +
                    "Ah4DCgAAAAAAOG11SwAAAAAAAAAAAAAAAA4AGAAAAAAAAAAQAO1BzwAAAHppcHRlc3QvZDEvZDIv" +
                    "VVQFAANrcxRadXgLAAEE6AMAAAToAwAAUEsBAh4DCgAAAAAAR211SwAAAAAAAAAAAAAAABEAGAAA" +
                    "AAAAAAAQAO1BFwEAAHppcHRlc3QvZDEvZDIvZDMvVVQFAAOFcxRadXgLAAEE6AMAAAToAwAAUEsB" +
                    "Ah4DCgAAAAAAR211Sweh6t0CAAAAAgAAABIAGAAAAAAAAQAAAKSBYgEAAHppcHRlc3QvZDEvZDIv" +
                    "ZDMvYVVUBQADhXMUWnV4CwABBOgDAAAE6AMAAFBLAQIeAwoAAAAAAFBtdUsAAAAAAAAAAAAAAAAO" +
                    "ABgAAAAAAAAAEADtQbABAAB6aXB0ZXN0L2QxL2Q1L1VUBQADmHMUWnV4CwABBOgDAAAE6AMAAFBL" +
                    "AQIeAwoAAAAAAFBtdUvE8sf2AgAAAAIAAAAPABgAAAAAAAEAAACkgfgBAAB6aXB0ZXN0L2QxL2Q1" +
                    "L2JVVAUAA5hzFFp1eAsAAQToAwAABOgDAABQSwECHgMKAAAAAABVbXVLQlWdoAIAAAACAAAADAAY" +
                    "AAAAAAABAAAApIFDAgAAemlwdGVzdC9kMS9kVVQFAAOicxRadXgLAAEE6AMAAAToAwAAUEsBAh4D" +
                    "CgAAAAAAU211S4XD3O8CAAAAAgAAAAwAGAAAAAAAAQAAAKSBiwIAAHppcHRlc3QvZDEvY1VUBQAD" +
                    "nXMUWnV4CwABBOgDAAAE6AMAAFBLBQYAAAAACgAKAEMDAADTAgAAAAA=";

}
