package io.github.mike10004.crxtool;

import org.junit.Test;

import java.io.InputStream;

public class CrxMetadataTest {

    /**
     * Makes sure toString() doesn't do something absurd like throw an exception.
     */
    @Test
    public void testToString() throws Exception {
        CrxMetadata metadata;
        try (InputStream in = Tests.getMakePageRedCrxResource().openStream()) {
            metadata = CrxParser.getDefault().parseMetadata(in);
        }
        System.out.println(metadata.toString());
    }

}