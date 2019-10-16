package io.github.mike10004.crxtool;

import java.util.List;

/**
 * Interface of an immutable value class that represents metadata plus stream annotations.
 */
public interface CrxInventory {

    /**
     * Gets the metadata associated with this instance.
     * @return the metadata
     */
    CrxMetadata metadata();

    /**
     * Gets the stream segment annotations made during parsing.
     * @return the stream segments
     */
    List<StreamSegment> streamSegments();

}

