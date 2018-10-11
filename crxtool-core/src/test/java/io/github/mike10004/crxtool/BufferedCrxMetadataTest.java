package io.github.mike10004.crxtool;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BufferedCrxMetadataTest {

    @Test
    public void testToString_2() throws Exception {
        testToString(2);
    }

    @Test
    public void testToString_3() throws Exception {
        testToString(3);
    }

    /**
     * Makes sure toString() doesn't do something absurd like throw an exception.
     */
    private void testToString(int version) throws IOException {
        URL resource = Tests.getMultiversionCrxExample().getResource(version);
        CrxMetadata metadata;
        try (InputStream in = resource.openStream()) {
            metadata = CrxParser.getDefault().parseMetadata(in);
        }
        System.out.println(metadata.toString());
    }

    @Test
    public void testEquals_version2() throws Exception {
        testEquals(Tests.getMultiversionCrxExample().getResource(2));
    }

    @Test
    public void testEquals_version3() throws Exception {
        testEquals(Tests.getMultiversionCrxExample().getResource(3));
    }

    @Test
    public void testNotEquals() throws Exception {
        CrxMetadata v2 = load(Tests.getMultiversionCrxExample().getResource(2));
        CrxMetadata v3 = load(Tests.getMultiversionCrxExample().getResource(3));
        assertNotEquals("same extension crx2 and crx3", v2, v3);
    }

    private static CrxMetadata load(URL resource) throws IOException {
        try (InputStream in = resource.openStream()) {
            return CrxParser.getDefault().parseMetadata(in);
        }
    }

    private void testEquals(URL resource) throws IOException {
        CrxMetadata m1 = load(resource);
        CrxMetadata m2 = load(resource);
        assertEquals("metadata equals", m1, m2);
    }
}