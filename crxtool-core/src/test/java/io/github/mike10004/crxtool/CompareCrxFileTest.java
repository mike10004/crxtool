package io.github.mike10004.crxtool;

import com.google.common.collect.Ordering;
import com.google.common.io.ByteSource;
import org.apache.commons.io.FileUtils;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class CompareCrxFileTest {

    @Test
    public void doCompare() throws Exception {
        doCompare(FileUtils.getTempDirectory());
    }

    private void doCompare(File dir) throws Exception {
        File officialFile = Tests.getAddFooterExtensionFile(CrxVersion.CRX3);
        CrxReading officialCrx = CrxReading.read(officialFile);
        write(officialFile, officialCrx);
        Tests.dumpCrxInfo(officialFile, System.out);
        File bootlegFile = java.nio.file.Files.walk(dir.toPath())
                .map(Path::toFile)
                .filter(f -> f.isFile() && f.getName().startsWith("BasicCrxPacker_WebDriverTest") && f.getName().endsWith(".crx"))
                .max(Ordering.natural().onResultOf(File::lastModified))
                .orElse(null);
        Assume.assumeTrue("no files in " + dir, bootlegFile != null);
        CrxReading bootlegCrx =  CrxReading.read(bootlegFile);
        write(bootlegFile, bootlegCrx);
        Tests.dumpCrxInfo(bootlegFile, System.out);
    }

    private void write(File crxFile, CrxReading reading) throws IOException {
        Map<String, ByteSource> slices = reading.sliceAtMarksUniquely();
        for (String label : slices.keySet()) {
            ByteSource slice = slices.get(label);
            write(slice, label, FileUtils.getTempDirectory().toPath(), crxFile.getName());
        }
    }

    private void write(ByteSource slice, String label, Path directory, String prefix) throws IOException {
        File dest = directory.resolve(String.format("%s.%s", prefix, label)).toFile();
        long length = slice.copyTo(com.google.common.io.Files.asByteSink(dest));
        System.out.format("%s written with %s bytes%n", dest, length);
    }
}
