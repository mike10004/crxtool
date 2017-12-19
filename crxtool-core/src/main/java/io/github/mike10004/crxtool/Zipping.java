package io.github.mike10004.crxtool;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;

public class Zipping {

    private Zipping() {}


    public static byte[] zipDirectory(Path extensionDir, @Nullable ZipConfig zipConfig) throws IOException {
        ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream(1024);
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(zipBuffer)) {
            java.nio.file.Files.walkFileTree(extensionDir, new ZippingFileVisitor(extensionDir, zipOutputStream));
            if (zipConfig != null) {
                if (zipConfig.comment != null) {
                    zipOutputStream.setComment(zipConfig.comment);
                }
                if (zipConfig.method != null) {
                    zipOutputStream.setMethod(zipConfig.method);
                }
                if (zipConfig.level != null) {
                    zipOutputStream.setLevel(zipConfig.level);
                }
            }
        }
        byte[] zipBytes = zipBuffer.toByteArray();
        return zipBytes;
    }
}
