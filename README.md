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

The example extension file in the test resources is from
https://developer.chrome.com/extensions/samples.

