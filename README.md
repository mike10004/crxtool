[![Travis build status](https://img.shields.io/travis/mike10004/crxtool.svg)](https://travis-ci.org/mike10004/crxtool)
[![AppVeyor build status](https://ci.appveyor.com/api/projects/status/bb3s40548ffj3uf5?svg=true)](https://ci.appveyor.com/project/mike10004/crxtool)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mike10004/crxtool.svg)](https://repo1.maven.org/maven2/com/github/mike10004/crxtool/)

# crxtool

Library for packing and unpacking Chrome extension `.crx` files.

## Core Library

### Maven Dependency Info

    <dependency>
        <groupId>com.github.mike10004</groupId>
        <artifactId>crxtool-core</artifactId>
        <version>0.7</version>
    </dependency>

### Usage

#### Unpacking

    try (InputStream in = new FileInputStream("my_extension.crx") {
        CrxMetadata metadata = CrxParser.getDefault().parseMetadata(in);
        System.out.println("id = " + metadata.id);
        // read the remainder of the stream into a byte array containing zipped data
        byte[] zipBytes = com.google.common.io.ByteStreams.toByteArray(in);
        // ...
    }

#### Packing

    Path extensionDir = new File("manifest-parent-dir").toPath();
    java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("RSA");
    java.security.SecureRandom random = new java.security.SecureRandom();
    keyGen.initialize(1024, random);
    java.security.KeyPair keyPair = keyGen.generateKeyPair();
    try (OutputStream out = new FileOutputStream("new_extension.crx")) {
        CrxPacker.getDefault().packExtension(extensionDir, keyPair, out);
    }

## Maven Plugin

### Maven Dependency Info

    <dependency>
        <groupId>com.github.mike10004</groupId>
        <artifactId>crxtool-maven-plugin</artifactId>
        <version>0.5</version>
    </dependency>

### Usage

Place extension source files in `src/main/extension`.

    <build>
        <plugins>
            <plugin>
                <groupId>com.github.mike10004</groupId>
                <artifactId>crxtool-maven-plugin</artifactId>
                <version>0.5</version>
                <executions>
                    <execution>
                        <id>pack</id>
                        <goals>
                            <goal>pack-extension</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>build-helper-maven-plugin</artifactId>
                <version>3.0.0</version>
                <executions>
                    <execution>
                        <id>attach-artifact</id>
                        <goals>
                            <goal>attach-artifact</goal>
                        </goals>
                        <configuration>
                            <artifacts>
                                <artifact>
                                    <file>${project.build.directory}/${project.artifactId}-${project.version}.crx</file>
                                    <type>crx</type>
                                </artifact>
                            </artifacts>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

## Credits

The extension ID construction is probably from [this Stack Overflow answer](https://stackoverflow.com/a/2050916/2657036). 
The make-page-red example extension file in the test resources is from [developer.chrome.com](https://developer.chrome.com/extensions/samples).
