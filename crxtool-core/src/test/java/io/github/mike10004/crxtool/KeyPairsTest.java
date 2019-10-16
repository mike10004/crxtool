package io.github.mike10004.crxtool;

import org.junit.Test;

import java.io.StringReader;
import java.security.KeyPair;

import static org.junit.Assert.*;

public class KeyPairsTest {

    @Test
    public void loadRsaKeyPairFromPrivateKeyBytes() throws Exception {
        String pem = TestingKey.getTestingKeyPemSource().read();
        byte[] privateKeyBytes = PemParser.getInstance().extractBytes(new StringReader(pem));
        KeyPair keyPair = KeyPairs.loadRsaKeyPairFromPrivateKeyBytes(privateKeyBytes);
        assertNotNull("keyPair", keyPair);
    }

}