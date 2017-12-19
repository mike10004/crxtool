package io.github.mike10004.crxtool;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import io.github.mike10004.crxtool.testing.Unzippage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ZippingFileVisitorTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void zipDirectory() throws Exception {
        Random random = new Random(ZippingFileVisitorTest.class.getName().hashCode());
        Path parent = temporaryFolder.newFolder().toPath();
        List<String> fileRelativePaths = Arrays.asList(
                "f1.dat", "d1/f2.dat", "d1/d1a/f3.dat", "d2/f4.dat"
        );
        Map<String, byte[]> groundTruth = new HashMap<>();
        int fileSize = 32;
        for (String relativePath : fileRelativePaths) {
            byte[] bytes = new byte[fileSize];
            File f = parent.resolve(relativePath).toFile();
            random.nextBytes(bytes);
            Files.createParentDirs(f);
            Files.write(bytes, f);
            groundTruth.put(relativePath, bytes);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream(fileRelativePaths.size() * fileSize);
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZippingFileVisitor visitor = new ZippingFileVisitor(parent, zos);
            java.nio.file.Files.walkFileTree(parent, visitor);
        }
        byte[] zipBytes = baos.toByteArray();
        File zipFile = File.createTempFile("add_footer", ".zip");
        Files.write(zipBytes, zipFile);
        Unzippage unzippage = Unzippage.unzip(new ByteArrayInputStream(zipBytes));
        Set<String> fileEntryNames = ImmutableSet.copyOf(unzippage.fileEntries());
        assertEquals("file entries", ImmutableSet.copyOf(fileRelativePaths), fileEntryNames);
        for (String entryName : fileEntryNames) {
            ByteSource actualBytes = unzippage.getFileBytes(entryName);
            assertNotNull("bytes present for " + entryName, actualBytes);
            assertTrue("content same", actualBytes.contentEquals(ByteSource.wrap(groundTruth.get(entryName))));
        }

        Path unpackedDir = temporaryFolder.newFolder().toPath();
        unzippage.extractTo(unpackedDir);
        Tests.DirDiff dirDiff = Tests.diffDirectories(parent, unpackedDir);
        assertTrue("dir diff empty", dirDiff.isEmpty());
    }
}