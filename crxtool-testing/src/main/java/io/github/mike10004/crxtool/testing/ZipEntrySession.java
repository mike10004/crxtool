package io.github.mike10004.crxtool.testing;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

interface ZipEntrySession {
    InputStream openStream() throws IOException;
    ZipEntry getEntry();
}
