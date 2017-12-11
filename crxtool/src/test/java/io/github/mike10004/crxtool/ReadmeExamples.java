package io.github.mike10004.crxtool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class ReadmeExamples {

    public static void main(String[] args) throws Exception {
        try (InputStream in = new FileInputStream("my_extension.crx")) {
            CrxMetadata metadata = CrxParser.getDefault().parseMetadata(in);
            System.out.println("id = " + metadata.id);
            // read the remainder of the stream into a byte array containing zipped data
            byte[] zipBytes = com.google.common.io.ByteStreams.toByteArray(in);
            // ...
        }

        Path extensionDir = new File("manifest-parent-dir").toPath();
        java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
        java.security.SecureRandom random = new java.security.SecureRandom();
        keyGen.initialize(1024, random);
        java.security.KeyPair keyPair = keyGen.generateKeyPair();
        try (OutputStream out = new FileOutputStream("new_extension.crx")) {
            CrxPacker.getDefault().packExtension(extensionDir, keyPair, out);
        }
    }
}
