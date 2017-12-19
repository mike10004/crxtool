package com.github.mike10004.crxtool.maven;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import io.github.mike10004.crxtool.CrxMetadata;
import io.github.mike10004.crxtool.CrxParser;
import io.github.mike10004.crxtool.KeyPairs;
import io.github.mike10004.crxtool.testing.Unzippage;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PackExtensionMojoTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File createPomStub(File basedir) throws IOException {
        File pomFile = new File(basedir, "pom.xml");
        java.nio.file.Files.write(pomFile.toPath(), Collections.singleton("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\n" +
                "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n" +
                "    <modelVersion>4.0.0</modelVersion>\n" +
                "    <groupId>com.example</groupId>\n" +
                "    <artifactId>crxtool-plugin-user</artifactId>\n" +
                "    <version>1.0-SNAPSHOT</version>\n" +
                "    <build>\n" +
                "        <plugins>\n" +
                "            <plugin>\n" +
                "                <groupId>com.github.mike10004</groupId>\n" +
                "                <artifactId>crxtool-maven-plugin</artifactId>\n" +
                "                <version>0.4-SNAPSHOT</version>\n" +
                "                <executions>\n" +
                "                    <execution>\n" +
                "                        <id>pack</id>\n" +
                "                    </execution>\n" +
                "                </executions>\n" +
                "            </plugin>\n" +
                "        </plugins>\n" +
                "    </build>\n" +
                "</project>"), StandardCharsets.UTF_8);
        return pomFile;
    }

    private MavenProject buildProject(File pomFile) throws IOException, XmlPullParserException {
        Model model;
        MavenXpp3Reader mavenreader = new MavenXpp3Reader();
        try (FileReader reader = new FileReader(pomFile)) {
            model = mavenreader.read(reader);
            model.setPomFile(pomFile);
        }
        MavenProject project = new MavenProject(model);
        return project;
    }

    private PackExtensionMojo buildMojo(File basedir) throws IOException, XmlPullParserException, URISyntaxException {
        File pomFile = createPomStub(basedir);
        MavenProject project = buildProject(pomFile);
        PackExtensionMojo mojo = new PackExtensionMojo();
        mojo.setProject(project);
        Path outputDir = temporaryFolder.newFolder().toPath();
        File crxtoolSampleExtensionDir = getSourceDirectory().toFile();
        mojo.setSourceDirectory(crxtoolSampleExtensionDir);
        mojo.setOutputFile(outputDir.resolve("extension.crx").toFile());
        return mojo;
    }

    @Test
    public void testExecute_crx_noPrivateKey() throws Exception {
        File crxFile = testExecute(false, null);
        checkMetadata(crxFile);
        checkZipDataInCrxFile(crxFile);
    }

    @Test
    public void testExecute_crx_privateKey() throws Exception {
        File pemFile = buildPemFile(KeyPairs.generateRsKeyPair(new SecureRandom()).getPrivate().getEncoded());
        File crxFile = testExecute(false, pemFile);
        checkMetadata(crxFile);
        checkZipDataInCrxFile(crxFile);
    }

    @Test
    public void testExecute_zip_noPrivateKey() throws Exception {
        File zipFile = testExecute(true, null);
        checkZipData(Files.asByteSource(zipFile).read());
    }

    @Test
    public void testExecute_zip_privateKey() throws Exception {
        File pemFile = buildPemFile(KeyPairs.generateRsKeyPair(new SecureRandom()).getPrivate().getEncoded());
        File zipFile = testExecute(true, pemFile);
        checkZipData(Files.asByteSource(zipFile).read());
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

    private File testExecute(boolean excludeHeader, @Nullable File privateKey) throws IOException, XmlPullParserException, MojoExecutionException, URISyntaxException {
        File basedir = temporaryFolder.newFolder();
        PackExtensionMojo mojo = buildMojo(basedir);
        mojo.setExcludeHeader(excludeHeader);
        mojo.setPrivateKey(privateKey);
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