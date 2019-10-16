package io.github.mike10004.crxtool;

import com.google.common.base.Verify;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class CrxReading {

    public final ByteSource bufferedCrxData;

    public final CrxInventory inventory;

    public CrxReading(ByteSource bufferedCrxData, CrxInventory inventory) {
        this.bufferedCrxData = bufferedCrxData;
        this.inventory = inventory;
    }

    public Map<String, ByteSource> sliceAtMarksUniquely() {
        Multimap<String, ByteSource> mm = sliceAtMarks();
        Verify.verify(mm.keySet().stream().map(mm::get).allMatch(v -> v.size() == 1), "slice labels must be unique in %s", mm);
        return Maps.transformValues(mm.asMap(), collection -> Objects.requireNonNull(collection).iterator().next());
    }

    public Multimap<String, ByteSource> sliceAtMarks() {
        Multimap<String, ByteSource> mm = ArrayListMultimap.create();
        for (StreamSegment mark : inventory.streamSegments()) {
            mm.put(mark.label(), bufferedCrxData.slice(mark.start(), mark.length()));
        }
        return mm;
    }

    public static CrxReading read(File crxFile) throws IOException {
        byte[] data = java.nio.file.Files.readAllBytes(crxFile.toPath());
        ByteSource bufferedData = ByteSource.wrap(data);
        CrxInventory inventory;
        try (InputStream in = bufferedData.openStream()) {
            inventory = CrxParser.getDefault().parseInventory(in);
        }
        return new CrxReading(bufferedData, inventory);
    }
}
