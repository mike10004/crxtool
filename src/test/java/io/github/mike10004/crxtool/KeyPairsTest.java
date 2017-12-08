package io.github.mike10004.crxtool;

import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.*;

public class KeyPairsTest {

    @Test
    public void loadRsaKeyPairFromPrivateKeyBytes() throws Exception {
        KeyPair keyPair = KeyPairs.loadRsaKeyPairFromPrivateKeyBytes(TestKey.getPrivateKeyBytes());
        assertNotNull("keyPair", keyPair);
    }

}