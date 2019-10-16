package io.github.mike10004.crxtool;

public interface StreamSegment {
    String label();
    long start();
    long end();
    default long length() {
        return end() - start();
    }
}
