package io.github.mike10004.crxtool;

import com.google.common.io.BaseEncoding;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.Reader;

class DefaultPemParser implements PemParser {

    private static final PemParser INSTANCE = new DefaultPemParser();

    public byte[] extractBytes(Reader reader) throws IOException {
        String base64 = CharStreams.readLines(reader, new Base64LineProcessor());
        return BaseEncoding.base64().decode(base64);
    }

    public static PemParser getInstance() {
        return INSTANCE;
    }

    private static class Base64LineProcessor implements LineProcessor<String> {

        private StringBuilder sb = new StringBuilder(4096 * 4 / 3);

        @SuppressWarnings("NullableProblems")
        @Override
        public boolean processLine(String line) {
            line = line.trim();
            if (!isMatchingCommentPattern(line)) {
                sb.append(line);
            }
            return true;
        }

        protected boolean isMatchingCommentPattern(String line) {
            return line.startsWith("---");
        }

        @Override
        public String getResult() {
            return sb.toString();
        }

    }

}
