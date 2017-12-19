package com.github.mike10004.crxtool.maven;

import com.google.common.io.BaseEncoding;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.Reader;

class PemParser {

    public byte[] extractBytes(Reader reader) throws IOException {
        String base64 = CharStreams.readLines(reader, new Base64LineProcessor());
        return BaseEncoding.base64().decode(base64);
    }

    static class Base64LineProcessor implements LineProcessor<String> {

        private StringBuilder sb = new StringBuilder(4096 * 4 / 3);

        @Override
        public boolean processLine(String line) throws IOException {
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
