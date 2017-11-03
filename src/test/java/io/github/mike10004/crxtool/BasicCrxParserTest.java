package io.github.mike10004.crxtool;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import io.github.mike10004.crxtool.Unzipper.UnzippedEntry;
import io.github.mike10004.crxtool.Unzipper.ZipReport;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;

public class BasicCrxParserTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void parseMetadata() throws Exception {
        BasicCrxParser parser = new BasicCrxParser();
        CrxMetadata metadata;
        ZipReport report;
        try (InputStream in = getClass().getResourceAsStream("/make_page_red.crx")) {
            metadata = parser.parseMetadata(in);
            report = new Unzipper().unzip(in, temporaryFolder.newFolder().toPath());
        }
        System.out.format("id=%s%npubkey=%s%nsignature=%s%n", metadata.id, metadata.pubkeyBase64, metadata.signatureBase64);
        assertEquals("id", "dolgciijajlmajahhgogoajoacjidnhi", metadata.id);
        report.unzippedEntries.forEach(entry -> {
            System.out.format("%s -> %s%n", entry.name, entry.file);
        });
        for (String filename : Arrays.asList("background.js", "manifest.json")) {
            Optional<UnzippedEntry> entryOpt = report.unzippedEntries.stream().filter(entry -> filename.equals(entry.name)).findFirst();
            assertTrue("not unzipped: " + filename, entryOpt.isPresent());
            URL reference = getClass().getResource("/make_page_red/" + filename);
            byte[] actualBytes = Files.toByteArray(entryOpt.get().file);
            byte[] expectedBytes = Resources.toByteArray(reference);
            assertArrayEquals("file bytes", expectedBytes, actualBytes);
        }
        assertTrue("manifest.json", report.unzippedEntries.stream().anyMatch(entry -> "manifest.json".equals(entry.name)));
        assertTrue("background.js", report.unzippedEntries.stream().anyMatch(entry -> "background.js".equals(entry.name)));

    }

}