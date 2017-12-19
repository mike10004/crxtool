package io.github.mike10004.crxtool.testing;

import javax.annotation.Nullable;
import java.io.IOException;

interface ZipFacade {
    @Nullable
    ZipEntrySession next() throws IOException;
}
