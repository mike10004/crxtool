package io.github.mike10004.crxtool;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import io.github.mike10004.crxtool.testing.Unzippage;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.zip.ZipFile;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class CrxPackerTestBase {

    @ClassRule
    public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected abstract CrxVersion getCrxVersion();

    public static class CrxTestCase {
        public final File referenceCrxFile;
        public final KeyPair keyPairUsedToSignFile;
        public final boolean expectSignaturesEqual;

        public CrxTestCase(File referenceCrxFile, KeyPair keyPairUsedToSignFile, boolean expectSignaturesEqual) {
            this.referenceCrxFile = referenceCrxFile;
            this.keyPairUsedToSignFile = keyPairUsedToSignFile;
            this.expectSignaturesEqual = expectSignaturesEqual;
        }
    }

    public static class PackResult {

        public final File crxFile;
        public final KeyPair keyPairUsedToSignFile;

        public PackResult(File crxFile, KeyPair keyPairUsedToSignFile) {
            this.crxFile = crxFile;
            this.keyPairUsedToSignFile = keyPairUsedToSignFile;
        }
    }

    protected abstract CrxTestCase loadPackExtensionTestCase() throws IOException, GeneralSecurityException, URISyntaxException;

    @Test
    public void packExtension() throws Exception {
        doPackExtensionTest();
    }

    protected PackResult doPackExtensionTest() throws GeneralSecurityException, IOException, URISyntaxException {
        CrxTestCase testCase = loadPackExtensionTestCase();
        File referenceCrxFile = testCase.referenceCrxFile;
        File extensionZipFile = Tests.chopZipFromCrx(referenceCrxFile);
        ByteSource extensionZip = Files.asByteSource(extensionZipFile);
        CrxPacker packer = createCrxPacker();
        KeyPair keyPair = testCase.keyPairUsedToSignFile;
        File extensionFile = File.createTempFile("crxtool-unit-test", ".crx", temporaryFolder.getRoot());
        try (OutputStream outputStream = new FileOutputStream(extensionFile)) {
            packer.packExtension(extensionZip::openStream, keyPair, outputStream);
        }
        CrxMetadata actualMetadata = readMetadata(extensionFile);
        CrxMetadata expectedMetadata = readMetadata(referenceCrxFile);
        assertEquals("magic number", expectedMetadata.getMagicNumber(), actualMetadata.getMagicNumber());
        assertEquals("version", expectedMetadata.getCrxVersion(), actualMetadata.getCrxVersion());
        AsymmetricKeyProof expectedProof = expectedMetadata.getFileHeader().getAsymmetricKeyProofs(MapFileHeader.ALGORITHM_SHA256_WITH_RSA).get(0);
        AsymmetricKeyProof actualProof = actualMetadata.getFileHeader().getAsymmetricKeyProofs(MapFileHeader.ALGORITHM_SHA256_WITH_RSA).get(0);
        assertEquals("pubkey.length", expectedProof.getPublicKeyLength(), actualProof.getPublicKeyLength());
        assertEquals("sig.length", expectedProof.getSignatureLength(), actualProof.getSignatureLength());
        assertEquals("pubkey", expectedProof.getPublicKeyBase64(), actualProof.getPublicKeyBase64());
        if (testCase.expectSignaturesEqual) {
            assertEquals("signature", expectedProof.getSignatureBase64(), actualProof.getSignatureBase64());
            assertTrue("crx bytes", Files.asByteSource(referenceCrxFile).contentEquals(Files.asByteSource(extensionFile)));
        }
        return new PackResult(extensionFile, keyPair);
    }

    @Test
    public void packExtensionFromDirCreatesValidMetadata() throws Exception {
        Path extensionDir = Tests.getAddFooterExtensionDir(getCrxVersion());
        File crxFile = File.createTempFile("BasicCrxPackerTest", ".crx", temporaryFolder.getRoot());
        ZipConfig zipConfig = new ZipConfig(null, null, "This is my comment");
        try (OutputStream output = new FileOutputStream(crxFile)) {
            createCrxPacker().packExtension(extensionDir, zipConfig, Tests.generateRsaKeyPair(getClass().hashCode()), output);
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

    private static String readMagicNumber(File file, @SuppressWarnings("SameParameterValue") int length) throws IOException {
        checkArgument(length > 0 && length <= 4, "magic number length invalid: %s", length);
        byte[] bytes = Files.asByteSource(file).slice(0, length).read();
        return StandardCharsets.US_ASCII.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
    }

    @Test
    public void packExtensionFromDirCreatesValidZip() throws Exception {
        Path extensionDir = Tests.getAddFooterExtensionDir(getCrxVersion());
        File crxFile = File.createTempFile("BasicCrxPackerTest", ".crx", temporaryFolder.getRoot());
        try (OutputStream output = new FileOutputStream(crxFile)) {
            createCrxPacker().packExtension(extensionDir, Tests.generateRsaKeyPair(getClass().hashCode()), output);
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

    protected abstract CrxPacker createCrxPacker();
}