package io.github.mike10004.crxtool.testing;

/*
    Requires these dependencies:

        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
            <version>23.6-jre</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.6</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-exec</artifactId>
            <version>1.3</version>
        </dependency>
 */

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class Chromedrivers {

    private static final Logger log = Logger.getLogger(Chromedrivers.class.getName());

    private Chromedrivers() {}

    private static class Compatibility {
        public final Range<Integer> chromeMajorVersionRange;
        public final String chromedriverVersion;
        private final BigDecimal numericChromedriverVersion;

        private Compatibility(Range<Integer> chromeMajorVersionRange, String chromedriverVersion) {
            this.chromeMajorVersionRange = requireNonNull(chromeMajorVersionRange);
            this.chromedriverVersion = requireNonNull(chromedriverVersion);
            checkArgument(!chromedriverVersion.trim().isEmpty());
            numericChromedriverVersion = new BigDecimal(chromedriverVersion);
        }
        private static final Ordering<Compatibility> orderingByChromedriverVersionAscending = Ordering.<BigDecimal>natural().onResultOf(compat -> compat.numericChromedriverVersion);

        public static Ordering<Compatibility> orderingByChromedriverVersion() {
            return orderingByChromedriverVersionAscending;
        }

        public static Compatibility of(String chromedriverVersion, int minChromeMajorInclusive, int maxChromeMajorInclusive) {
            return new Compatibility(Range.closed(minChromeMajorInclusive, maxChromeMajorInclusive), chromedriverVersion);
        }
    }

    private static class CompatibleVersionFinder {
        public final ImmutableList<Compatibility> compatibilityList;

        private CompatibleVersionFinder(Iterable<Compatibility> compatibilityList) {
            this.compatibilityList = Compatibility.orderingByChromedriverVersion().reverse().immutableSortedCopy(compatibilityList);
        }

        @Nullable
        public String findNewestCompatibleChromedriverVersion(int chromeMajorVersion) {
            for (Compatibility compatibility : compatibilityList) {
                if (compatibility.chromeMajorVersionRange.contains(chromeMajorVersion)) {
                    return compatibility.chromedriverVersion;
                }
            }
            return null;
        }
    }

    // https://stackoverflow.com/a/49618567/2657036
    //        2.36            63-65
    //        2.35            62-64
    //        2.34            61-63
    //        2.33            60-62
    //        ---------------------
    //        2.28            57+
    //        2.25            54+
    //        2.24            53+
    //        2.22            51+
    //        2.19            44+
    //        2.15            42+

    private static final ImmutableList<Compatibility> COMPATIBILITY_TABLE = ImmutableList.<Compatibility>builder()
            .add(Compatibility.of("2.37", 64, 66))
            .add(Compatibility.of("2.36", 63, 65))
            .add(Compatibility.of("2.35", 62, 64))
            .add(Compatibility.of("2.34", 61, 63))
            .add(Compatibility.of("2.33", 60, 62))
            .add(Compatibility.of("2.32", 57, 62))
            // info before that is foggy; see https://sites.google.com/a/chromium.org/chromedriver/downloads
            .build();

    private static CompatibleVersionFinder FINDER_INSTANCE = new CompatibleVersionFinder(COMPATIBILITY_TABLE);

    private static CompatibleVersionFinder getFinderInstance() {
        return FINDER_INSTANCE;
    }

    /**
     * Determines the best Chromedriver version for the installed version of Google Chrome.
     * @return the version string, or null if not detectable
     */
    @Nullable
    public static String determineBestChromedriverVersion() {
        ChromeExecutableResolver executableResolver = SystemUtils.IS_OS_WINDOWS
                ? new WindowsChromeExecutableResolver()
                : new PathVariableSearchingResolver();
        @Nullable File chromeExecutable = executableResolver.resolve();
        if (chromeExecutable != null) {
            for (VersionCapturer versionCapturer : _ExecutingChromeVersionQuerier.versionCapturers) {
                @Nullable String chromeVersionString = versionCapturer.captureVersion(chromeExecutable);
                if (chromeVersionString != null) {
                    try {
                        int chromeMajorVersion = parseChromeMajorVersion(chromeVersionString);
                        return getFinderInstance().findNewestCompatibleChromedriverVersion(chromeMajorVersion);
                    } catch (RuntimeException e) {
                        log.log(Level.INFO, "failed to parse major version from {0} due to {1}", new Object[]{StringEscapeUtils.escapeJava(StringUtils.abbreviate(chromeVersionString, 256)), e});
                    }
                }
            }
            log.log(Level.WARNING, "none of these worked: {0}", _ExecutingChromeVersionQuerier.versionCapturers);
        }
        return null;
    }

    private static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.whitespace()).omitEmptyStrings().trimResults();
    private static final Splitter DOT_SPLITTER = Splitter.on('.').omitEmptyStrings();

    public static int parseChromeMajorVersion(String chromeVersionString) {
        Iterable<String> tokens = WHITESPACE_SPLITTER.split(chromeVersionString);
        for (String token : tokens) {
            if (token.matches("^\\d+(\\.\\d+)+")) {
                return Integer.parseInt(DOT_SPLITTER.split(token).iterator().next());
            }
        }
        //noinspection deprecation
        throw new IllegalArgumentException(String.format("no tokens look like a version in \"%s\"", StringEscapeUtils.escapeJava(StringUtils.abbreviate(chromeVersionString, 256))));
    }

    interface ChromeExecutableResolver {
        @Nullable
        File resolve();
    }

    private interface VersionCapturer {

        boolean isRunnable();

        @Nullable
        String captureVersion(File chromeExecutable);
    }

    private static class ChromeExecutingVersionCapturer implements VersionCapturer {

        @Override
        public boolean isRunnable() {
            return !SystemUtils.IS_OS_WINDOWS;
        }

        @Nullable
        @Override
        public String captureVersion(File chromeExecutable) {
            String value = execute(chromeExecutable, "--no-sandbox", "--disable-gpu", "--version");
            if (value != null) {
                log.log(Level.INFO, "chrome version: {0}", String.format("\"%s\"", StringEscapeUtils.escapeJava(value)));
            } else {
                log.log(Level.WARNING, "failed to capture version from chrome executable {0}", chromeExecutable);
            }
            return value;
        }
    }

    /*
     * https://bugs.chromium.org/p/chromium/issues/detail?id=158372#c13
     */
    private static class WmicVersionCapturer implements VersionCapturer {
        @Override
        public boolean isRunnable() {
            return SystemUtils.IS_OS_WINDOWS;
        }

        @Nullable
        @Override
        public String captureVersion(File chromeExecutable) {
            String wmicOutput = execute("wmic", "datafile", "where", "name=" + chromeExecutable.getAbsolutePath(), "get", "Version", "/value");
            wmicOutput = StringUtils.trim(wmicOutput);
            wmicOutput = StringUtils.removeStart(wmicOutput, "Version=");
            return wmicOutput;
        }
    }

    /*
     * https://stackoverflow.com/questions/30686/get-file-version-in-powershell
     */
    private static class PowershellVersionCapturer implements VersionCapturer {

        @Override
        public boolean isRunnable() {
            return SystemUtils.IS_OS_WINDOWS;
        }

        @Nullable
        @Override
        public String captureVersion(File chromeExecutable) {
            String chromeExecutablePath = chromeExecutable.getAbsolutePath().replace('\\', '/');
            String powershellArg = String.format("(Get-Item \"%s\").VersionInfo.FileVersion", chromeExecutablePath);
            return execute("powershell", powershellArg);
        }
    }

    private static class _ExecutingChromeVersionQuerier  {

        private static final ImmutableList<VersionCapturer> versionCapturers = ImmutableList.<VersionCapturer>builder()
                    .add(new ChromeExecutingVersionCapturer())
                    .add(new PowershellVersionCapturer())
                    .add(new WmicVersionCapturer())
                    .build();

    }

    private static final int PROCESS_EXECUTION_TIMEOUT_MILLIS = 10000;

    @Nullable
    private static String execute(File executable, String...args) {
        return execute(executable.getAbsolutePath(), args);
    }

    @Nullable
    private static String execute(String executable, String...args) {
        Executor executor = new DefaultExecutor();
        executor.setExitValues(null);
        CommandLine commandLine = new CommandLine(executable);
        Stream.of(args).forEach(commandLine::addArgument);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(PROCESS_EXECUTION_TIMEOUT_MILLIS);
        ByteArrayOutputStream stdoutBucket = new ByteArrayOutputStream(128);
        ByteArrayOutputStream stderrBucket = new ByteArrayOutputStream(128);
        Charset charset = Charset.defaultCharset();
        PumpStreamHandler streamHandler = new PumpStreamHandler(stdoutBucket, stderrBucket);
        executor.setStreamHandler(streamHandler);
        executor.setWatchdog(watchdog);
        try {
            int exitCode = executor.execute(commandLine);
            String stdout = new String(stdoutBucket.toByteArray(), charset);
            String stderr = new String(stderrBucket.toByteArray(), charset);
            if (exitCode == 0) {
                if (stdout.trim().isEmpty()) {
                    log.log(Level.INFO, "stdout is empty; stderr: {0}", stderr);
                }
                return stdout;
            } else {
                log.log(Level.WARNING, "executing {0} with arguments {1} failed: {2}", new Object[]{executable, Arrays.toString(args), stderr});
            }
        } catch (IOException e) {
            log.log(Level.WARNING, "failed to await termination of {0} process after {1} millis: {2}", new Object[]{executable, PROCESS_EXECUTION_TIMEOUT_MILLIS, e.toString()});
        }
        return null;
    }

    private static class WindowsChromeExecutableResolver implements ChromeExecutableResolver {

        private static final String REG_KEY = "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe";

        private static List<File> buildLikelyPathsList() {
            File userExecutable = new File(System.getProperty("user.home")).toPath()
                    .resolve("AppData")
                    .resolve("Local")
                    .resolve("Google")
                    .resolve("Chrome")
                    .resolve("Application")
                    .resolve("chrome.exe")
                    .toFile();
            List<File> systemRoots = Arrays.asList(new File("C:/Program Files/"), new File("C:/Program Files (x86)/"));
            Stream<File> systemExePaths = systemRoots.stream().map(root -> {
                return root.toPath().resolve("Google").resolve("Chrome").resolve("Application").resolve("chrome.exe").toFile();
            });
            return Stream.concat(Stream.of(userExecutable), systemExePaths).collect(Collectors.toList());
        }

        @Nullable
        @Override
        public File resolve() {
            // %UserProfile%\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe
            List<File> likelyExePaths = buildLikelyPathsList();
            for (File chromeExe : likelyExePaths) {
                if (chromeExe.isFile() && chromeExe.canExecute()) {
                    return chromeExe;
                }
            }
            return queryRegistryForExePath();
        }

        protected File queryRegistryForExePath() {
            String regOutput = execute("reg", "QUERY", REG_KEY);
            if (regOutput != null) {
                //                                                             C:\path\to\...
                Matcher m = Pattern.compile("\\s*\\(Default\\)\\s+REG_SZ\\s+(\\w:.+)\\s*$").matcher(regOutput);
                if (m.find()) {
                    File pathname = new File(m.group(1));
                    if (pathname.isFile() && pathname.canExecute()) {
                        return pathname;
                    }
                }
            }
            return null;
        }
    }

    private static class PathVariableSearchingResolver implements ChromeExecutableResolver {

        private static final Logger log = Logger.getLogger(PathVariableSearchingResolver.class.getName());

        private static final ImmutableSet<String> CHROME_EXECUTABLE_NAMES = ImmutableSet.of("google-chrome", "chromium-browser", "chrome");

        @Override
        public File resolve() {
            for (String chromeExecutableName : CHROME_EXECUTABLE_NAMES) {
                String path = findByNameOnSystemPath(chromeExecutableName);
                if (path != null) {
                    try {
                        return new File(path);
                    } catch (Exception e) {
                        log.log(Level.INFO, "failed to resolve chrome executable by name {0}: {1}", new Object[]{path, e});
                    }
                }
            }
            return null;
        }

        @Nullable
        protected String findByNameOnSystemPath(String input) {
            try {
                Set<String> names = new HashSet<>();
                names.add(input);
                if (SystemUtils.IS_OS_WINDOWS) {
                    input = input.toLowerCase();
                    if (!input.matches("^.+\\.\\S{3}")) {
                        names.add(input + ".exe");
                    }
                }
                List<String> systemPathDirectories = Splitter.on(File.pathSeparatorChar).omitEmptyStrings()
                        .splitToList(Strings.nullToEmpty(System.getenv("PATH")));
                for (String dir : systemPathDirectories) {
                    for (String name : names) {
                        File f = new File(dir, name);
                        if (f.isFile() && f.canExecute()) {
                            return f.getAbsolutePath();
                        }
                    }
                }
            } catch (RuntimeException e) {
                log.log(Level.WARNING, "failed to which {0}: {1}", new Object[]{input, e});
            }
            return null;
        }

    }
}
