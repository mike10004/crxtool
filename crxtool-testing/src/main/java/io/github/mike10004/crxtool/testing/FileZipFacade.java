package io.github.mike10004.crxtool.testing;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class FileZipFacade implements ZipFacade {

    private final ZipFile zipFile;
    private final Iterator<? extends ZipEntry> entries;

    public FileZipFacade(ZipFile zipFile) {
        entries = zipFile.stream().iterator();
        this.zipFile = zipFile;
    }

    @Nullable
    @Override
    public ZipEntrySession next() {
        if (entries.hasNext()) {
            ZipEntry entry = entries.next();
            return new ZipEntrySession() {
                @Override
                public ZipEntry getEntry() {
                    return entry;
                }

                @Override
                public InputStream openStream() throws IOException {
                    return zipFile.getInputStream(entry);
                }
            };
        } else {
            return null;
        }
    }

}
