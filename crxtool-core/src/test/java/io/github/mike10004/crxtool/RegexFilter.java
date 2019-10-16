package io.github.mike10004.crxtool;

import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegexFilter implements LineProcessor<List<String>> {

    private final Pattern pattern;
    private List<String> groups = Collections.emptyList();

    public RegexFilter(Pattern pattern) {
        this.pattern = pattern;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean processLine(String line) {
        Matcher m = pattern.matcher(line);
        if (m.find()) {
            groups = IntStream.range(0, m.groupCount() + 1).mapToObj(m::group).collect(Collectors.toList());
            return false;
        }
        return true;
    }

    @Override
    public List<String> getResult() {
        return groups;
    }
}
