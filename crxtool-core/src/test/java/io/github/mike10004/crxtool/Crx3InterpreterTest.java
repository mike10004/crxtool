package io.github.mike10004.crxtool;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertEquals;

public class Crx3InterpreterTest {

    private static final String EXAMPLE_ID = "bpfnehkjjffiihnbadbgpfpmedcpojjl";

    @Test
    public void sameId() throws Exception {
        String resourcePath = String.format("/%s/example.crx3", EXAMPLE_ID);
        CrxMetadata metadata3 = testCrxResource(resourcePath);
        CrxMetadata metadata2 = loadVersion2();
        checkState(metadata3.getVersion() == 3, "this test only makes sense if the version is 3");
        assertEquals("id", metadata2.getId(), metadata3.getId());
    }

    private CrxMetadata loadVersion2() throws Exception {
        String resourcePath = String.format("/%s/example.crx2", EXAMPLE_ID);
        CrxMetadata metadata = testCrxResource(resourcePath);
        checkState(metadata.getVersion() == 2, "this test only makes sense if the version is 2");
        return metadata;
    }

    private CrxMetadata testCrxResource(String resourcePath) throws IOException {
        URL resource = getClass().getResource(resourcePath);
        checkState(resource != null, "classpath:%s not found", resourcePath);
        CrxMetadata metadata;
        try (InputStream in = resource.openStream()) {
            metadata = CrxParser.getDefault().parseMetadata(in);
        }
        System.out.format("%s parsed from %s%n", metadata, resourcePath);
        return metadata;
    }

}