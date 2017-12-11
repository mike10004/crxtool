package io.github.mike10004.crxtool;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BasicCrxParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void parseMetadata() throws Exception {
        BasicCrxParser parser = new BasicCrxParser();
        CrxMetadata metadata;
        Unzippage unzippage;
        try (InputStream in = Tests.getMakePageRedCrxResource().openStream()) {
            metadata = parser.parseMetadata(in);
            unzippage = Unzippage.unzip(in);
        }
        System.out.format("headerLength=%s%nid=%s%npubkey=%s%nsignature=%s%n", metadata.headerLength(), metadata.id, metadata.pubkeyBase64, metadata.signatureBase64);
        assertEquals("id", "dnogaomdbgfngjgalaoggcfahgeibfdc", metadata.id);
        unzippage.allEntries().forEach(entry -> {
            System.out.format("%s%n", entry);
        });
        for (String filename : Arrays.asList("background.js", "manifest.json")) {
            ByteSource entryBytes = unzippage.getFileBytes(filename);
            assertNotNull("entry bytes present expected", entryBytes);
            URL reference = getClass().getResource("/make_page_red/" + filename);
            byte[] actualBytes = entryBytes.read();
            byte[] expectedBytes = Resources.toByteArray(reference);
            assertArrayEquals("file bytes", expectedBytes, actualBytes);
        }
    }

}