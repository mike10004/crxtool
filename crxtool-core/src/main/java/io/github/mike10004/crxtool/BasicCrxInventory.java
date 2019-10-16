package io.github.mike10004.crxtool;

import java.util.List;

class BasicCrxInventory implements CrxInventory {

    private final CrxMetadata metadata;
    private final List<ParsingState.Mark> marks;

    BasicCrxInventory(CrxMetadata metadata, List<ParsingState.Mark> marks) {
        this.metadata = metadata;
        this.marks = marks;
    }

    @Override
    public CrxMetadata metadata() {
        return metadata;
    }

    @Override
    public List<ParsingState.Mark> marks() {
        return marks;
    }
}
