package io.github.mike10004.crxtool;

import com.google.common.io.ByteStreams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class Crx3PackerTest extends CrxPackerTestBase {

    @Override
    protected CrxVersion getCrxVersion() {
        return CrxVersion.CRX3;
    }

    @Override
    protected CrxTestCase loadPackExtensionTestCase() throws IOException, GeneralSecurityException, URISyntaxException {
        String keyPem = Tests.getTestingKeyPemSource().read();
        KeyPair keyPair = KeyPairs.loadRsaKeyPairFromPrivateKeyBytes(PemParser.getInstance().extractBytes(new StringReader(keyPem)));
        File referenceCrxFile = new File(Tests.getMakePageRedCrxResource(CrxVersion.CRX3).toURI());
        return new CrxTestCase(referenceCrxFile, keyPair, false);
    }

    @Override
    protected CrxPacker createCrxPacker() {
        return new Crx3Packer();
    }

    @Override
    public void packExtension() throws Exception {
        PackResult packResult = super.doPackExtensionTest();
        File crxFile = packResult.crxFile;
        byte[] zipBytes;
        CrxMetadata metadata;
        try (InputStream in = java.nio.file.Files.newInputStream(crxFile.toPath())) {
            metadata = new BasicCrxParser().parseMetadata(in);
            zipBytes = ByteStreams.toByteArray(in);
        }
        assertEquals(CrxVersion.CRX3, metadata.getCrxVersion());
        List<AsymmetricKeyProof> proofs = metadata.getFileHeader()
                .getAsymmetricKeyProofs(CrxProofAlgorithm.sha256_with_rsa);
        assertEquals("expect exactly 1 sha256_with_rsa proof", 1, proofs.size());
        AsymmetricKeyProof proof = proofs.get(0);
        assertEquals("public key base64", KeyPairs.encodePublicKeyBase64(packResult.keyPairUsedToSignFile), proof.getPublicKeyBase64());
    }
}
