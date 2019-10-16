package io.github.mike10004.crxtool;

import java.util.List;

interface ParsingState {

    SegmentMark markStart(String key);

    List<StreamSegment> dump();

}

