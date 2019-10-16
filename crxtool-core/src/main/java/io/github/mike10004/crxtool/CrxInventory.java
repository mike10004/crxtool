package io.github.mike10004.crxtool;

import java.util.List;

public interface CrxInventory {

    CrxMetadata metadata();

    List<StreamSegment> streamSegments();

}

