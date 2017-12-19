package com.github.mike10004.crxtool.maven;

import com.github.mike10004.crxtool.maven.PackExtensionMojo.PrivateKeyParameterConflictException;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import io.github.mike10004.crxtool.CrxMetadata;
import io.github.mike10004.crxtool.CrxParser;
import io.github.mike10004.crxtool.KeyPairs;
import io.github.mike10004.crxtool.testing.Unzippage;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PackExtensionMojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testExecute_crx_noPrivateKey() throws Exception {
        File crxFile = testExecute(false, null, false);
        checkMetadata(crxFile);
        checkZipDataInCrxFile(crxFile);
    }

    @Test
    public void testExecute_crx_privateKey() throws Exception {
        File pemFile = buildPemFile(KeyPairs.generateRsKeyPair(new SecureRandom()).getPrivate().getEncoded());
        File crxFile = testExecute(false, pemFile, false);
        checkMetadata(crxFile);
        checkZipDataInCrxFile(crxFile);
    }

    @Test
    public void testExecute_crx_privateKeyFileSpecifiedButAbsent() throws Exception {
        try {
            File pemFile = new File(temporaryFolder.newFolder(), "mykey.pem");
            testExecute(false, pemFile, false);
        } catch (MojoExecutionException e) {
            assertTrue(e.getCause() instanceof FileNotFoundException);
        }
    }

    @Test
    public void testExecute_crx_privateKeyFileSpecifiedButAbsent_ignoreAbsent() throws Exception {
        File pemFile = new File(temporaryFolder.newFolder(), "mykey.pem");
        File crxFile = testExecute(false, pemFile, true);
        checkMetadata(crxFile);
        checkZipDataInCrxFile(crxFile);
    }

    @Test
    public void testExecute_zip_noPrivateKey() throws Exception {
        File zipFile = testExecute(true, null, false);
        checkZipData(Files.asByteSource(zipFile).read());
    }

    @Test(expected = PrivateKeyParameterConflictException.class)
    public void testExecute_zip_privateKey() throws Exception {
        File pemFile = temporaryFolder.newFile();
        testExecute(true, pemFile, false);
    }

    private File buildPemFile(byte[] privateKey) throws IOException {
        BaseEncoding encoder = BaseEncoding.base64().withSeparator("\n", 72);
        File file = temporaryFolder.newFile();
        String base64 = encoder.encode(privateKey);
        List<String> lines = Arrays.asList("-----BEGIN RSA PRIVATE KEY-----", base64, "-----END RSA PRIVATE KEY-----");
        java.nio.file.Files.write(file.toPath(), lines, StandardCharsets.US_ASCII);
        return file;
    }

    private Path getSourceDirectory() throws URISyntaxException {
        return new File(getClass().getResource("/sample-extension-1/manifest.json").toURI()).getParentFile().toPath();
    }

    private PackExtensionMojo buildMojo() throws IOException, URISyntaxException {
        PackExtensionMojo mojo = new PackExtensionMojo();
        Path outputDir = temporaryFolder.newFolder().toPath();
        File crxtoolSampleExtensionDir = getSourceDirectory().toFile();
        mojo.setSourceDirectory(crxtoolSampleExtensionDir);
        mojo.setOutputFile(outputDir.resolve("extension.crx").toFile());
        return mojo;
    }

    private File testExecute(boolean excludeHeader, @Nullable File privateKey, boolean generateKey) throws IOException, MojoExecutionException, URISyntaxException {
        PackExtensionMojo mojo = buildMojo();
        mojo.setExcludeHeader(excludeHeader);
        mojo.setPrivateKey(privateKey);
        mojo.setGenerateKeyIfAbsent(generateKey);
        mojo.execute();
        File outputFile = mojo.getOutputFile();
        assertTrue("file length", outputFile.length() > 0);
        return outputFile;

    }

    private void checkMetadata(File outputFile) throws IOException {
        CrxMetadata metadata;
        try (InputStream in = new FileInputStream(outputFile)) {
            metadata = CrxParser.getDefault().parseMetadata(in);
        }
        assertEquals("metadata", "Cr24", metadata.magicNumber);
    }

    private void checkZipDataInCrxFile(File crxFile) throws IOException {
        try (InputStream in = new FileInputStream(crxFile)) {
            CrxParser.getDefault().parseMetadata(in);
            checkZipData(ByteStreams.toByteArray(in));
        }
    }

    private void checkZipData(byte[] zipData) throws IOException {
        Unzippage unzippage;
        try (InputStream in = new ByteArrayInputStream(zipData)) {
            unzippage = Unzippage.unzip(in);
        }
        assertEquals("filepaths", ImmutableSet.of("manifest.json", "background.js"), ImmutableSet.copyOf(unzippage.fileEntries()));
    }


}