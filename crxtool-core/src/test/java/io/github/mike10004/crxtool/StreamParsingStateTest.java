package io.github.mike10004.crxtool;

import com.google.common.io.CountingInputStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class StreamParsingStateTest {

    @Test
    public void testMarkOne_autoCloseable() throws Exception {

        byte[] data = "Cr24abcdefghijklmnopqrstuv".getBytes(StandardCharsets.US_ASCII);
        CountingInputStream in = new CountingInputStream(new ByteArrayInputStream(data));
        StreamParsingState state = StreamParsingState.fromStream(in);
        String magicNumber = new BasicCrxParser().readMagicNumber(in, state);
        assertEquals("Cr24", magicNumber);
        List<StreamSegment> marks = state.dump();
        assertEquals(1, marks.size());
        StreamSegment actual = marks.get(0);
        assertEquals("magicNumber", actual.label());
        assertEquals(0, actual.start());
        assertEquals(4, actual.end());
        assertEquals(4, actual.length());
    }

}