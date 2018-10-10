Source: https://chromium.googlesource.com/chromium/src.git/+/62.0.3178.1/components/crx_file/crx3.proto
Retrieved: 2018-10-10

To regenerate Java source files from *.proto files, download protoc from
https://github.com/protocolbuffers/protobuf/releases and execute

    $ protoc --java_out=${project.basedir}/src/main/java *.proto

The generated source file is committed to the repository so that we don't
have to make `protoc` a prerequisite.
