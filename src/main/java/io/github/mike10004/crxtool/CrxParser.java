package io.github.mike10004.crxtool;

import java.io.IOException;
import java.io.InputStream;

public interface CrxParser {

    CrxMetadata parseMetadata(InputStream crxInputStream) throws IOException;

}
