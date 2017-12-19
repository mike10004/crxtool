package io.github.mike10004.crxtool.testing;

import com.google.common.math.LongMath;

public class UnzipConfig {

    public static final int DEFAULT_DEFAULT_ENTRY_BUFFER_INITIAL_CAPACITY = 256;
    public static final int DEFAULT_ENTRY_BUFFER_MAX_LENGTH = Integer.MAX_VALUE;
    public static final long DEFAULT_MAX_TOTAL_UNCOMPRESSED_SIZE = LongMath.checkedMultiply(Integer.MAX_VALUE, 2);

    public final int defaultEntryBufferInitialCapacity;
    public final int entryBufferMaxLength;
    public final long maxTotalUncompressedSize;

    private static UnzipConfig DEFAULT = builder().build();

    private UnzipConfig(Builder builder) {
        defaultEntryBufferInitialCapacity = builder.defaultEntryBufferInitialCapacity;
        entryBufferMaxLength = builder.entryBufferMaxLength;
        maxTotalUncompressedSize = builder.maxTotalUncompressedSize;
    }

    public static UnzipConfig getDefault() {
        return DEFAULT;
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("unused")
    public static final class Builder {

        private int defaultEntryBufferInitialCapacity = DEFAULT_DEFAULT_ENTRY_BUFFER_INITIAL_CAPACITY;
        private int entryBufferMaxLength = DEFAULT_ENTRY_BUFFER_MAX_LENGTH;
        private long maxTotalUncompressedSize = DEFAULT_MAX_TOTAL_UNCOMPRESSED_SIZE;

        private Builder() {
        }

        public Builder defaultEntryBufferInitialCapacity(int val) {
            defaultEntryBufferInitialCapacity = val;
            return this;
        }

        public Builder entryBufferMaxLength(int val) {
            entryBufferMaxLength = val;
            return this;
        }

        public Builder maxTotalUncompressedSize(long val) {
            maxTotalUncompressedSize = val;
            return this;
        }

        public UnzipConfig build() {
            return new UnzipConfig(this);
        }
    }
}
