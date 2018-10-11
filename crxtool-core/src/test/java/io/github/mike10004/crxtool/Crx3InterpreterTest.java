package io.github.mike10004.crxtool;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.google.common.base.Preconditions.checkState;
import static org.junit.Assert.assertEquals;

public class Crx3InterpreterTest {

    @Test
    public void sameId() throws Exception {
        CrxMetadata metadata3 = testCrxResource(Tests.getMultiversionCrxExample().getResource(3));
        CrxMetadata metadata2 = loadVersion2();
        checkState(metadata3.getVersion() == 3, "this test only makes sense if the version is 3");
        assertEquals("id", metadata2.getId(), metadata3.getId());
    }

    private CrxMetadata loadVersion2() throws Exception {
        CrxMetadata metadata = testCrxResource(Tests.getMultiversionCrxExample().getResource(2));
        checkState(metadata.getVersion() == 2, "this test only makes sense if the version is 2");
        return metadata;
    }

    private CrxMetadata testCrxResource(URL resource) throws IOException {
        CrxMetadata metadata;
        try (InputStream in = resource.openStream()) {
            metadata = CrxParser.getDefault().parseMetadata(in);
        }
        System.out.format("%s parsed from %s%n", metadata, resource);
        return metadata;
    }

}