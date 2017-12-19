package io.github.mike10004.crxtool.testing;

import javax.annotation.Nullable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import static com.google.common.base.Preconditions.checkArgument;

public class CommonsStreamFacade implements ZipFacade {

    private ZipArchiveInputStream zipInputStream;

    public CommonsStreamFacade(InputStream in) {
        checkArgument(!(in instanceof java.util.zip.ZipInputStream));
        checkArgument(!(in instanceof ZipArchiveInputStream));
        this.zipInputStream = new ZipArchiveInputStream(in);
    }

    @Nullable
    @Override
    public ZipEntrySession next() throws IOException {
        ZipArchiveEntry entry = zipInputStream.getNextZipEntry();
        return entry == null ? null : new CommonsEntryFacade(zipInputStream, entry);
    }

    public static class CommonsEntryFacade implements ZipEntrySession {

        private ZipArchiveInputStream inputStream;
        private ZipArchiveEntry entry;

        public CommonsEntryFacade(ZipArchiveInputStream inputStream, ZipArchiveEntry entry) {
            this.inputStream = Objects.requireNonNull(inputStream, "inputStream");
            this.entry = Objects.requireNonNull(entry, "entry");
        }

        @Override
        public InputStream openStream() {
            return new FilterInputStream(inputStream) {
                @Override
                public void close() {
                }
            };
        }

        @Override
        public ZipEntry getEntry() {
            return entry;
        }
    }
}
