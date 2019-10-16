package io.github.mike10004.crxtool;

import org.junit.Test;

public class TestingKeyTest {

    @Test
    public void load() throws Exception {
        TestingKey.getInstance().loadTestingKeyPair(); // no exception means ok
    }
}
