package io.github.mike10004.crxtool;

import com.google.common.io.BaseEncoding;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.assertNotNull;

public class TestingKeyTest {

    @Test
    public void load() throws Exception {
        String resourcePath = "/key-for-testing.rsa.gz";
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            throw new FileNotFoundException("classpath:" + resourcePath);
        }
        String pemString;
        try (Reader reader = new InputStreamReader(new GZIPInputStream(resource.openStream()), StandardCharsets.US_ASCII)) {
            pemString = CharStreams.toString(reader);
        }
        PrivateKey privateKey = new JmeterPrivateKeyReader().getPrivateKey(new StringReader(pemString));
        PublicKey publicKey = KeyPairs.extractPublicKey(privateKey);
        System.out.println(BaseEncoding.base64().encode(publicKey.getEncoded()));
        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        assertNotNull(keyPair);
    }
}
