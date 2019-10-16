package io.github.mike10004.crxtool;

import org.junit.Test;

import java.io.File;

public class Crx3Packer_WebDriverTest extends CrxPacker_WebDriverTestBase {

    @Override
    protected CrxPacker createPacker() {
        return new Crx3Packer();
    }

    @Override
    public void packAndUseExtension() throws Exception {
        super.packAndUseExtension();
    }

    @Test
    public void packAndUseExtension_canned() throws Exception {
        File crxFile = Tests.getAddFooterExtensionFile(CrxVersion.CRX3);
        packAndUseExtension(crxFile);
    }

}
