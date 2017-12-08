package io.github.mike10004.crxtool;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

/**
 * Value class that represents options for compressing data in ZIP format.
 */
public class ZipConfig {

    /**
     * The method, or null if the default method is to be used.
     * @see ZipOutputStream#setMethod(int)
     */
    @Nullable
    public final Integer method;

    /**
     * The compression level, or null if the default level is to be used.
     * @see ZipOutputStream#setLevel(int)
     */
    @Nullable
    public final Integer level;
    @Nullable
    public final String comment;

    /**
     * Constructs an instance of the class.
     * @param method the compression method
     * @param level the compression
     * @param comment a comment
     * @see #method
     * @see #level
     * @see #comment
     */
    public ZipConfig(@Nullable Integer method, @Nullable Integer level, @Nullable String comment) {
        this.method = method;
        this.level = level;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "ZipConfig{" +
                "method=" + method +
                ", level=" + level +
                ", comment='" + comment + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZipConfig zipConfig = (ZipConfig) o;
        return Objects.equals(method, zipConfig.method) &&
                Objects.equals(level, zipConfig.level) &&
                Objects.equals(comment, zipConfig.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, level, comment);
    }
}
