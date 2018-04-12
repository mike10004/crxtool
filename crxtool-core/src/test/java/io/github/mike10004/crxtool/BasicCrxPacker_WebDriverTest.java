package io.github.mike10004.crxtool;

import com.github.mike10004.xvfbtesting.XvfbRule;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.mike10004.crxtool.testing.Chromedrivers;
import io.github.mike10004.nanochamp.server.NanoControl;
import io.github.mike10004.nanochamp.server.NanoResponse;
import io.github.mike10004.nanochamp.server.NanoServer;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static org.junit.Assert.assertEquals;

public class BasicCrxPacker_WebDriverTest {

    @Rule
    public XvfbRule xvfb = XvfbRule.builder().build();

    @BeforeClass
    public static void configureChromedriver() {
        @Nullable String chromedriverVersion = Chromedrivers.determineBestChromedriverVersion();
        WebDriverManager m = ChromeDriverManager.getInstance();
        if (chromedriverVersion != null) {
            m.version(chromedriverVersion);
        }
        m.setup();
    }

    @Test
    public void packAndUseExtension() throws Exception {
        ChromeDriverService driverService = new ChromeDriverService.Builder()
                .withEnvironment(xvfb.getController().newEnvironment())
                .build();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        File crxFile = packExtension();
        options.addExtensions(crxFile);
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head><title>Test Page</title></head>" +
                "<body>This is a test</body>" +
                "</html>";
        NanoServer server = NanoServer.builder()
                .get(NanoResponse.status(200).htmlUtf8(html))
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
        }
    }

    private File packExtension() throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Path extensionDir = Tests.getAddFooterExtensionDir();
        File extensionFile = File.createTempFile("BasicCrxPacker_WebDriverTest", ".crx");
        try (OutputStream output = new FileOutputStream(extensionFile)) {
            new BasicCrxPacker().packExtension(extensionDir, Tests.generateRsaKeyPair(getClass().hashCode()), output);
        }
        return extensionFile;
    }

}
