package io.github.mike10004.crxtool;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.zip.ZipOutputStream;

public class ZipConfig {

    @Nullable
    public final Integer method;
    @Nullable
    public final Integer level;
    @Nullable
    public final String comment;

    private static final ZipConfig DEFAULT_INSTANCE = new ZipConfig(null, null, null);

    public static ZipConfig getDefault() {
        return DEFAULT_INSTANCE;
    }

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
