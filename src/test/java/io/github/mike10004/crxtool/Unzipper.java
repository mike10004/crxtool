package io.github.mike10004.crxtool;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.math.IntMath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.requireNonNull;

public class Unzipper {

    public static class ZipReport {

        public final int numFileEntriesClaimed;
        public final ImmutableList<UnzippedEntry> unzippedEntries;

        public ZipReport(int numFileEntriesClaimed, ImmutableList<UnzippedEntry> unzippedEntries) {
            this.unzippedEntries = requireNonNull(unzippedEntries);
            this.numFileEntriesClaimed = numFileEntriesClaimed;
        }
    }

    public static class UnzippedEntry {
        public final String name;
        public final File file;
        public final long compressedLength;
        public final long decompressedLength;

        public UnzippedEntry(String name, File file, long compressedLength, long decompressedLength) {
            this.name = name;
            this.file = file;
            this.compressedLength = compressedLength;
            this.decompressedLength = decompressedLength;
        }

        public static UnzippedEntry fromZipEntry(ZipEntry entry, File outputFile) {
            return new UnzippedEntry(entry.getName(), outputFile, entry.getCompressedSize(), entry.getSize());
        }
    }

    public ZipReport unzip(InputStream input, Path directory) throws IOException {
        ZipInputStream zip = new ZipInputStream(input);
        ZipEntry entry;
        ImmutableList.Builder<UnzippedEntry> entries = ImmutableList.builder();
        int numEntriesClaimed = 0;
        while ((entry = zip.getNextEntry()) != null) {
            try {
                Path entryPath = directory.resolve(entry.getName());
                try {
                    if (entry.isDirectory()) {
                        mkdirs(entryPath);
                    } else {
                        numEntriesClaimed = IntMath.checkedAdd(numEntriesClaimed, 1);
                        File entryOutputFile = entryPath.toFile();
                        mkdirs(entryOutputFile.getParentFile());
                        try (OutputStream fileOut = new FileOutputStream(entryOutputFile)) {
                            long numBytesDecompressed = ByteStreams.copy(zip, fileOut);
                            if (numBytesDecompressed != entry.getSize()) {
                                throw new IOException(String.format("decompressed %s bytes but expected %s in entry %s", numBytesDecompressed, entry.getSize(), entry.getName()));
                            }
                        }
                        entries.add(UnzippedEntry.fromZipEntry(entry, entryOutputFile));
                    }
                } catch (IOException e) {
                    handleEntryException(entry, e);
                }
            } finally {
                zip.closeEntry();
            }
        }
        return new ZipReport(numEntriesClaimed, entries.build());
    }

    @SuppressWarnings("unused")
    protected void handleEntryException(ZipEntry entry, IOException exception) throws IOException {
        throw exception;
    }

    private static void mkdirs(Path path) throws IOException {
        mkdirs(path.toFile());
    }

    private static void mkdirs(File f) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        f.mkdirs();
        if (!f.isDirectory()) {
            throw new IOException("failed to make directory: " + f);
        }
    }
}
