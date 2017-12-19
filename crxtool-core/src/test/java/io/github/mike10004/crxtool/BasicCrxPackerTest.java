package io.github.mike10004.crxtool;

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteSource;
import com.google.common.io.CountingOutputStream;
import com.google.common.io.Files;
import com.google.common.io.LittleEndianDataOutputStream;
import io.github.mike10004.crxtool.testing.Unzippage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Random;
import java.util.zip.ZipFile;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicCrxPackerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void packExtension() throws Exception {
        File referenceCrxFile = new File(Tests.getMakePageRedCrxResource().toURI());
        File extensionZipFile = Tests.chopZipFromCrx(referenceCrxFile);
        ByteSource extensionZip = Files.asByteSource(extensionZipFile);
        BasicCrxPacker packer = new BasicCrxPacker();
        KeyPair keyPair = KeyPairs.loadRsaKeyPairFromPrivateKeyBytes(TestKey.getPrivateKeyBytes());
        File extensionFile = File.createTempFile("crxtool-unit-test", ".crx");
        try (OutputStream outputStream = new FileOutputStream(extensionFile)) {
            packer.packExtension(extensionZip, keyPair, outputStream);
        }
        CrxMetadata actualMetadata = readMetadata(extensionFile);
        CrxMetadata expectedMetadata = readMetadata(referenceCrxFile);
        assertEquals("magic number", expectedMetadata.magicNumber, actualMetadata.magicNumber);
        assertEquals("version", expectedMetadata.version, actualMetadata.version);
        assertEquals("pubkey.length", expectedMetadata.pubkeyLength, actualMetadata.pubkeyLength);
        assertEquals("sig.length", expectedMetadata.signatureLength, actualMetadata.signatureLength);
        assertEquals("pubkey", expectedMetadata.pubkeyBase64, actualMetadata.pubkeyBase64);
        assertEquals("sig", expectedMetadata.signatureBase64, actualMetadata.signatureBase64);
        assertTrue("crx bytes", Files.asByteSource(referenceCrxFile).contentEquals(Files.asByteSource(extensionFile)));
    }

    @Test
    public void packExtensionFromDirCreatesValidMetadata() throws Exception {
        Path extensionDir = Tests.getAddFooterExtensionDir();
        File crxFile = File.createTempFile("BasicCrxPackerTest", ".crx", temporaryFolder.getRoot());
        ZipConfig zipConfig = new ZipConfig(null, null, "This is my comment");
        try (OutputStream output = new FileOutputStream(crxFile)) {
            new BasicCrxPacker().packExtension(extensionDir, zipConfig, Tests.generateRsaKeyPair(getClass().hashCode()), output);
        }
        String magic = readMagicNumber(crxFile, 4);
        checkState("Cr24".equals(magic), "magic number incorrect: %s", magic);
        CrxMetadata metadata;
        try (InputStream in = new FileInputStream(crxFile)) {
            metadata = CrxParser.getDefault().parseMetadata(in);
        }
        System.out.format("metadata: %s%n", metadata);
        File zipFile = Tests.chopZipFromCrx(crxFile);
        String actualComment;
        try (ZipFile zf = new ZipFile(zipFile)) {
            actualComment = zf.getComment();
        }
        assertEquals("zip comment", zipConfig.comment, actualComment);
    }

    private static String readMagicNumber(File file, int length) throws IOException {
        checkArgument(length > 0 && length <= 4, "magic number length invalid: %s", length);
        byte[] bytes = Files.asByteSource(file).slice(0, length).read();
        return StandardCharsets.US_ASCII.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
    }

    @Test
    public void packExtensionFromDirCreatesValidZip() throws Exception {
        Path extensionDir = Tests.getAddFooterExtensionDir();
        File crxFile = File.createTempFile("BasicCrxPackerTest", ".crx");
        try (OutputStream output = new FileOutputStream(crxFile)) {
            new BasicCrxPacker().packExtension(extensionDir, Tests.generateRsaKeyPair(getClass().hashCode()), output);
        }
        String magic = readMagicNumber(crxFile, 4);
        checkState("Cr24".equals(magic), "magic number incorrect: %s", magic);
        File zipFile = Tests.chopZipFromCrx(crxFile);
        Path unpackedDir = temporaryFolder.newFolder().toPath();
        Unzippage unzippage = Unzippage.unzip(zipFile);
        unzippage.extractTo(unpackedDir);
        Tests.DirDiff diff = Tests.diffDirectories(extensionDir, unpackedDir);
        if (!diff.isEmpty()) {
            diff.dump(System.out);
        }
        assertTrue("diff empty", diff.isEmpty());
    }

    private CrxMetadata readMetadata(File crxFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(crxFile)) {
            return CrxParser.getDefault().parseMetadata(inputStream);
        }
    }

    private static final String FORMAT_VERSION_HEX = "02000000";

    @Test
    public void writeFormatVersion() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(32);
        CountingOutputStream counter = new CountingOutputStream(buffer);
        LittleEndianDataOutputStream output = new LittleEndianDataOutputStream(counter);
        new BasicCrxPacker().writeFormatVersion(output);
        output.flush();
        long count = counter.getCount();
        assertEquals("byte count", 4, count);
        byte[] expected = BaseEncoding.base16().decode(FORMAT_VERSION_HEX);
        byte[] actual = buffer.toByteArray();
//        System.out.format("%s%n", BaseEncoding.base16().encode(actual));
        assertArrayEquals("version encoding", expected, actual);
    }

    @Test
    public void writeExtensionHeader() throws Exception {
        Random random = new Random(getClass().getName().hashCode());
        byte[] publicKeyBytes = new byte[1024];
        random.nextBytes(publicKeyBytes);
        byte[] signature = new byte[2048];
        random.nextBytes(signature);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        new BasicCrxPacker().writeExtensionHeader(publicKeyBytes, signature, baos);
        baos.flush();
        byte[] headerBytes = baos.toByteArray();
        CrxMetadata metadata = CrxParser.getDefault().parseMetadata(new ByteArrayInputStream(headerBytes));
        assertEquals("pubkey length", publicKeyBytes.length, metadata.pubkeyLength);
        assertEquals("sig length", signature.length, metadata.signatureLength);
    }
}