package io.github.mike10004.crxtool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

class InputSources {

    private InputSources() {}

    public static class BufferedInputSource implements InputSource {

        private final byte[] data;

        public BufferedInputSource(byte[] data) {
            this.data = requireNonNull(data);
        }

        @Override
        public InputStream openStream() {
            return new ByteArrayInputStream(data);
        }

        @Override
        public byte[] read() {
            return Arrays.copyOf(data, data.length);
        }

        @Override
        public String toString() {
            return String.format("BufferedInputSource{data[%d]}", data.length);
        }
    }
}
