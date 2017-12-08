[![Travis build status](https://img.shields.io/travis/mike10004/crxtool.svg)](https://travis-ci.org/mike10004/crxtool)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mike10004/crxtool.svg)](https://repo1.maven.org/maven2/com/github/mike10004/crxtool/)

# crxtool

Library for packing and unpacking Chrome extension `.crx` files.

## Usage

### Unpacking

    try (InputStream in = new FileInputStream("my_extension.crx") {
        CrxMetadata metadata = CrxParser.getDefault().parseMetadata(in);
        System.out.println("id = " + metadata.id);
        // read the remainder of the stream into a byte array containing zipped data
        byte[] zipBytes = com.google.common.io.ByteStreams.toByteArray(in);
        // ...
    }

### Packing

    Path extensionDir = new File("manifest-parent-dir").toPath();
    java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
    java.security.SecureRandom random = new java.security.SecureRandom();
    keyGen.initialize(1024, random);
    java.security.KeyPair keyPair = keyGen.generateKeyPair();
    try (OutputStream out = new FileOutputStream("new_extension.crx")) {
        CrxPacker.getDefault().packExtension(extensionDir, keyPair, out);
    }

## Credits

The extension ID construction is probably from [this Stack Overflow answer](https://stackoverflow.com/a/2050916/2657036). 
The make-page-red example extension file in the test resources is from [developer.chrome.com](https://developer.chrome.com/extensions/samples).
