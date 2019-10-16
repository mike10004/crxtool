package io.github.mike10004.crxtool;

import com.github.mike10004.xvfbtesting.XvfbRule;
import com.google.common.base.Strings;
import com.google.common.io.CharSource;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public abstract class CrxPacker_WebDriverTestBase {

    @Rule
    public XvfbRule xvfb = XvfbRule.builder().build();

    @BeforeClass
    public static void configureChromedriver() {
        WebDriverManager.chromedriver().setup();
    }

    @Test
    public void packAndUseExtension() throws Exception {
        File crxFile = packExtension();
        packAndUseExtension(crxFile);
    }

    protected void packAndUseExtension(File crxFile) throws Exception {
        Tests.dumpCrxInfo(crxFile, System.out);
        ChromeDriverService driverService = new ChromeDriverService.Builder()
                .withEnvironment(xvfb.getController().newEnvironment())
                .build();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addExtensions(crxFile);
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head><title>Test Page</title></head>" +
                "<body>This is a test</body>" +
                "</html>";
        NanoServer.ResponseProvider nanoResponder = x -> NanoResponse.status(200).htmlUtf8(html);
        NanoServer server = NanoServer.builder()
                .get(nanoResponder)
                .build();
        try (NanoControl control = server.startServer()) {
            WebDriver driver = new ChromeDriver(driverService, options);
            try {
                driver.get(control.baseUri().toString());
                WebElement injectedContentElement = new WebDriverWait(driver, 3, 100).until(ExpectedConditions.presenceOfElementLocated(By.id("injected-content")));
                String text = injectedContentElement.getText().trim();
                System.out.format("content injected by extension: %s%n", text);
                assertEquals("injected content text", "hello, world", text);
            } finally {
                driver.quit();
            }
        } catch (WebDriverException e) {
            String msg = e.getMessage();
            if (Strings.nullToEmpty(msg).startsWith("unknown error: cannot process extension #1")) {
                List<String> found = CharSource.wrap(msg).readLines(new RegexFilter(Pattern.compile("CRX verification failed: (\\d+)")));
                if (found.size() > 1) {
                    int enumIndex = Integer.parseInt(found.get(1));
                    VerifierResult verifierResult = VerifierResult.values()[enumIndex];
                    System.err.format("Chrome failed to verify extension; code: %s%n", verifierResult);
                }
            }
            throw e;
        }
    }

    /**
     * Copied from Chromium source code: https://chromium.googlesource.com/chromium/src.git/+/62.0.3178.1/components/crx_file/crx_verifier.h?autodive=0%2F%2F
     */
    @SuppressWarnings("unused")
    enum VerifierResult {
        OK_FULL,   // The file verifies as a correct full CRX file.
        OK_DELTA,  // The file verifies as a correct differential CRX file.
        ERROR_FILE_NOT_READABLE,      // Cannot open the CRX file.
        ERROR_HEADER_INVALID,         // Failed to parse or understand CRX header.
        ERROR_EXPECTED_HASH_INVALID,  // Expected hash is not well-formed.
        ERROR_FILE_HASH_FAILED,       // The file's actual hash != the expected hash.
        ERROR_SIGNATURE_INITIALIZATION_FAILED,  // A signature or key is malformed.
        ERROR_SIGNATURE_VERIFICATION_FAILED,    // A signature doesn't match.
        ERROR_REQUIRED_PROOF_MISSING,           // RequireKeyProof was unsatisfied.
    };

    protected abstract CrxPacker createPacker();

    private File packExtension() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        CrxPacker packer = createPacker();
        Path extensionDir = Tests.getAddFooterExtensionDir(packer.getCrxVersion());
        File extensionFile = File.createTempFile("BasicCrxPacker_WebDriverTest", ".crx");
        try (OutputStream output = new FileOutputStream(extensionFile)) {
            KeyPair keyPair = TestingKey.getInstance().loadTestingKeyPair();
            createPacker().packExtension(extensionDir, keyPair, output);
        }
        return extensionFile;
    }

}
