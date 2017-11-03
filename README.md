[![Travis build status](https://img.shields.io/travis/mike10004/crxtool.svg)](https://travis-ci.org/mike10004/crxtool)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mike10004/crxtool.svg)](https://repo1.maven.org/maven2/com/github/mike10004/crxtool/)

# crxtool

Library for reading and unpacking Chrome extension `.crx` files.

## Usage

    try (InputStream in = new FileInputStream("my_extension.crx") {
        CrxMetadata metadata = new BasicCrxParser().parseMetadata(in);
        System.out.println("id = " + metadata.id);
        // read the remainder of the stream into a byte array containing zipped data
        byte[] zipBytes = com.google.common.io.ByteStreams.toByteArray(in);
        // ...
    }

## Credits

The extension ID construction is probably from [this Stack Overflow answer](https://stackoverflow.com/a/2050916/2657036). 
The example extension file in the test resources is from [developer.chrome.com](https://developer.chrome.com/extensions/samples).
