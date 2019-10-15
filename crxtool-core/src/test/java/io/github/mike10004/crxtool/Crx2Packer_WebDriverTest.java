package io.github.mike10004.crxtool;

public class Crx2Packer_WebDriverTest extends CrxPacker_WebDriverTestBase {
    @Override
    protected CrxPacker createPacker() {
        return new Crx2Packer();
    }
}
