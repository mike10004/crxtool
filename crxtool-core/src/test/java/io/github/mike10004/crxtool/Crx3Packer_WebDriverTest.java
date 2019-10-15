package io.github.mike10004.crxtool;

public class Crx3Packer_WebDriverTest extends CrxPacker_WebDriverTestBase {
    @Override
    protected CrxPacker createPacker() {
        return new Crx3Packer();
    }
}
