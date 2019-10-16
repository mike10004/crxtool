package io.github.mike10004.crxtool;

import java.util.Collections;
import java.util.List;

class EmptyParsingState implements ParsingState {

    private static final SegmentMark SCOPE = new EmptySegmentMark();

    private static final EmptyParsingState INSTANCE = new EmptyParsingState();

    public static ParsingState getInstance() {
        return INSTANCE;
    }

    @Override
    public SegmentMark markStart(String key) {
        return SCOPE;
    }

    @Override
    public List<StreamSegment> dump() {
        return Collections.emptyList();
    }

    private static class EmptySegmentMark implements SegmentMark {

        @Override
        public void close() {
        }
    }

}
