package io.github.mike10004.crxtool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.zip.GZIPInputStream;

public class Crx3PackerTest extends CrxPackerTestBase {

    @Override
    protected CrxVersion getCrxVersion() {
        return CrxVersion.CRX3;
    }

    @Override
    protected CrxTestCase loadPackExtensionTestCase() throws IOException, GeneralSecurityException, URISyntaxException {
        String resourcePath = "/key-for-testing.rsa.gz";
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            throw new FileNotFoundException("classpath:" + resourcePath);
        }
        PrivateKey privateKey;
        try (Reader reader = new InputStreamReader(new GZIPInputStream(resource.openStream()), StandardCharsets.US_ASCII)) {
            privateKey = new JmeterPrivateKeyReader().getPrivateKey(reader);
        }
        PublicKey publicKey = KeyPairs.extractPublicKey(privateKey);
        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        File referenceCrxFile = new File(Tests.getMakePageRedCrxResource(CrxVersion.CRX3).toURI());
        return new CrxTestCase(referenceCrxFile, keyPair, false);
    }

    @Override
    protected CrxPacker createCrxPacker() {
        return new Crx3Packer();
    }

}
