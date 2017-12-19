package io.github.mike10004.crxtool.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import static java.util.Objects.requireNonNull;

/**
 * Class that represents the result of unzipping a file. Use {@link #unzip(File)} or
 * {@link #unzip(InputStream)} create an instance.
 */
public abstract class Unzippage {

    protected Unzippage() {}

    /**
     * Returns an iterable over the names of zip entries that represent compressed files.
     * @return the file entries
     */
    public abstract Iterable<String> fileEntries();

    /**
     * Returns an iterable over the names of zip entries that represent directories.
     * @return the directory entries
     */
    public abstract Iterable<String> directoryEntries();

    /**
     * Returns a byte source that supplies a stream containing the decompressed
     * bytes of a zip entry.
     * @param fileEntry the file entry
     * @return the byte source, or null if the argument does not specify a file entry
     */
    @Nullable
    public abstract ByteSource getFileBytes(String fileEntry);

    public Iterable<String> allEntries() {
        return Iterables.concat(directoryEntries(), fileEntries());
    }

    private static class CollectionUnzippage extends Unzippage {

        private final ImmutableList<String> directoryEntries;
        private final ImmutableMap<String, ByteSource> fileEntries;

        protected CollectionUnzippage(Iterable<String> directoryEntries, Map<String, ByteSource> fileEntries) {
            this.directoryEntries = ImmutableList.copyOf(directoryEntries);
            this.fileEntries = ImmutableMap.copyOf(fileEntries);
        }

        @Override
        public Iterable<String> fileEntries() {
            return fileEntries.keySet();
        }

        @Override
        public Iterable<String> directoryEntries() {
            return directoryEntries;
        }

        @Override
        @Nullable
        public ByteSource getFileBytes(String fileEntry) {
            return fileEntries.get(fileEntry);
        }
    }

    /**
     * Unzips data from a stream using the default unzip configuration.
     * @see #unzip(InputStream, UnzipConfig)
     * @see UnzipConfig#getDefault()
     */
    public static Unzippage unzip(InputStream inputStream) throws IOException {
        return unzip(inputStream, UnzipConfig.getDefault());
    }

    /**
     * Unzips data from an input stream. The stream must be open and positioned
     * at the beginning of the zip data.
     * @param inputStream the input stream
     * @return the unzippage
     * @throws IOException if something goes awry
     */
    public static Unzippage unzip(InputStream inputStream, UnzipConfig config) throws IOException {
        return unzip(new CommonsStreamFacade(inputStream), config);
    }

    /**
     * Unzips a file with the default unzip configuration.
     * @see #unzip(File, UnzipConfig)
     * @see UnzipConfig#getDefault()
     */
    public static Unzippage unzip(File zipPathname) throws IOException {
        return unzip(zipPathname, UnzipConfig.getDefault());
    }

    /**
     * Unzips a zip file.
     * @param zipPathname the pathname of the zip file
     * @return the unzippage
     * @throws IOException if something goes awry
     */
    public static Unzippage unzip(File zipPathname, UnzipConfig config) throws IOException {
        return unzip(zipPathname, config, ZipIntegrityConstraint.CHECK_INTEGRITY);
    }

    public static Unzippage unzip(File zipPathname, UnzipConfig config, ZipIntegrityConstraint integrityConstraint) throws IOException {
        if (integrityConstraint == ZipIntegrityConstraint.CHECK_INTEGRITY) {
            try (InputStream in = new FileInputStream(zipPathname)) {
                return unzip(in, config);
            }
        } else {
            try (ZipFile zipFile = new ZipFile(zipPathname)) {
                return unzip(new FileZipFacade(zipFile), config);
            }
        }
    }

    public static class UnzipException extends ZipException {
        public UnzipException(String s) {
            super(s);
        }
    }

    static Unzippage unzip(ZipFacade entryProvider, UnzipConfig config) throws IOException {
        List<String> directoryEntries = new ArrayList<>();
        Map<String, byte[]> fileEntries = new HashMap<>();
        ZipEntrySession session;
        long totalUncompressedSize = 0;
        while ((session = entryProvider.next()) != null) {
            ZipEntry entry = requireNonNull(session.getEntry(), "session.getEntry()");
            if (entry.isDirectory()) {
                directoryEntries.add(entry.getName());
            } else {
                int bufferLen = Ints.checkedCast(Math.max(entry.getCompressedSize(), entry.getSize()));
                if (bufferLen > config.entryBufferMaxLength) {
                    throw new UnzipException("entry size greater than max buffer length: " + bufferLen);
                }
                if (bufferLen <= 0) {
                    bufferLen = config.defaultEntryBufferInitialCapacity;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferLen);
                long inLimit = LongMath.checkedAdd(config.entryBufferMaxLength, 1);
                try (InputStream input = ByteStreams.limit(session.openStream(), inLimit)) {
                    long numCopied = ByteStreams.copy(input, baos);
                    if (numCopied == inLimit) {
                        throw new UnzipException("entry size limit breached: " + numCopied);
                    }
                    totalUncompressedSize = LongMath.checkedAdd(totalUncompressedSize, numCopied);
                    if (totalUncompressedSize > config.maxTotalUncompressedSize) {
                        throw new UnzipException("max total uncompressed size breached: " + totalUncompressedSize);
                    }
                }
                baos.flush();
                fileEntries.put(entry.getName(), baos.toByteArray());
            }
        }
        return new CollectionUnzippage(directoryEntries, Maps.transformValues(fileEntries, ByteSource::wrap));
    }

    public void extractTo(Path parent) throws IOException {
        for (String fileEntry : fileEntries()) {
            File file = parent.resolve(fileEntry).toFile();
            com.google.common.io.Files.createParentDirs(file);
            ByteSource bs = requireNonNull(getFileBytes(fileEntry));
            bs.copyTo(com.google.common.io.Files.asByteSink(file));
        }
    }
}