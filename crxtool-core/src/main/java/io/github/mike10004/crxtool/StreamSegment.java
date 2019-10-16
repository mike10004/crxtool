package io.github.mike10004.crxtool;

/**
 * Interface that represents an annotation of a stream segment.
 */
public interface StreamSegment {

    /**
     * Label for the segment.
     * @return the label
     */
    String label();

    /**
     * Gets the start position of the segment in the stream.
     * @return start position (inclusive)
     */
    long start();

    /**
     * Gets the end position (exclusive) of the segment in the stream.
     * This is the position of the first byte after the segment.
     * @return end position (exclusive)
     */
    long end();

    /**
     * Gets the length of the stream segment.
     * @return length of this stream segment
     */
    default long length() {
        return end() - start();
    }
}
