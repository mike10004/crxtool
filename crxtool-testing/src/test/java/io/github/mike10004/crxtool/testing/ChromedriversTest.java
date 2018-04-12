package io.github.mike10004.crxtool.testing;

import com.google.common.collect.ImmutableMap;
import org.junit.Assume;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ChromedriversTest {

    @Test
    public void findBestVersion() {
        String chromedriverVersion = Chromedrivers.determineBestChromedriverVersion();
        Assume.assumeTrue("chromedriver version not detected", chromedriverVersion != null);
    }

    @Test
    public void parseChromeMajorVersion() {
        Map<String, Integer> testCases = ImmutableMap.<String, Integer>builder()
                .put("Google Chrome 62.0.3202.94", 62)
                .put("Chromium 62.0.3202.94 Built on Ubuntu , running on Ubuntu 14.04", 62)
                .build();
        testCases.forEach((input, expected) -> {
            int actual = Chromedrivers.parseChromeMajorVersion(input);
            assertEquals("on input " + input, expected.intValue(), actual);
        });
    }
}