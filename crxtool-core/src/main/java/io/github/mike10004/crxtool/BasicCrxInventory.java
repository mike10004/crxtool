package io.github.mike10004.crxtool;

import java.util.List;

class BasicCrxInventory implements CrxInventory {

    private final CrxMetadata metadata;
    private final List<StreamSegment> marks;

    BasicCrxInventory(CrxMetadata metadata, List<StreamSegment> marks) {
        this.metadata = metadata;
        this.marks = marks;
    }

    @Override
    public CrxMetadata metadata() {
        return metadata;
    }

    @Override
    public List<StreamSegment> streamSegments() {
        return marks;
    }
}
