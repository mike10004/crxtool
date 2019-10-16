package io.github.mike10004.crxtool;

import com.google.common.io.CharSource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.zip.GZIPInputStream;

public class TestingKey {

    private final CharSource pemSource;

    private TestingKey(CharSource pemSource) {
        this.pemSource = pemSource;
    }

    public static TestingKey getInstance() {
        return new TestingKey(getTestingKeyPemSource());
    }

    public String getIdPmdecimal() {
        return "fpmlidmjhnfgbgnfhofnbbiacmmmcdgl";
    }

    public KeyPair loadTestingKeyPair() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        CharSource source = getTestingKeyPemSource();
        byte[] pemBytes;
        try (Reader reader = source.openStream()) {
            pemBytes = PemParser.getInstance().extractBytes(reader);
        }
        return KeyPairs.loadRsaKeyPairFromPrivateKeyBytes(pemBytes);
    }

    public byte[] loadTestingKeyPrivateKeyBytes() throws IOException {
        String pem = getTestingKeyPemSource().read();
        byte[] privateKeyBytes = PemParser.getInstance().extractBytes(new StringReader(pem));
        return privateKeyBytes;
    }

    public static CharSource getTestingKeyPemSource() {
        String resourcePath = "/key-for-testing.pem.gz";
        URL testingKeyGzipped = Tests.class.getResource(resourcePath);
        if (testingKeyGzipped == null) {
            throw new IllegalStateException("testing key not found at classpath:" + resourcePath);
        }
        return new CharSource() {
            @Override
            public Reader openStream() throws IOException {
                return new InputStreamReader(new GZIPInputStream(testingKeyGzipped.openStream()), StandardCharsets.US_ASCII);
            }
        };
    }
}
