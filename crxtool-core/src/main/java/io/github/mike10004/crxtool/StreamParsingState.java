package io.github.mike10004.crxtool;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CountingInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongSupplier;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

class StreamParsingState implements ParsingState {

    private volatile MySegmentMark scope;
    private final LongSupplier positionGetter;
    private final List<StreamSegment> completedMarks = new ArrayList<>();

    public StreamParsingState(LongSupplier positionGetter) {
        this.positionGetter = requireNonNull(positionGetter);
    }

    public static StreamParsingState fromStream(CountingInputStream in) {
        return new StreamParsingState(in::getCount);
    }

    private static class PartialMark {

        public final String label;
        public final long start;

        PartialMark(String label, long start) {
            this.label = label;
            this.start = start;
        }

        public StreamSegment complete(long end) {
            return new StreamMark(label, start, end);
        }
    }

    private class MySegmentMark implements SegmentMark {

        private final PartialMark partial;

        private MySegmentMark(PartialMark partial) {
            this.partial = partial;
        }

        @Override
        public final void close() {
            closeAndReturnCompletedMark();
        }

        @SuppressWarnings("UnusedReturnValue")
        public final StreamSegment closeAndReturnCompletedMark() {
            checkState(this == scope, "this is not the current scope");
            scope = null;
            long position = positionGetter.getAsLong();
            StreamSegment completed = partial.complete(position);
            completedMarks.add(completed);
            return completed;
        }

    }

    @Override
    public synchronized SegmentMark markStart(String key) {
        long position = positionGetter.getAsLong();
        if (scope != null) {
            scope.close();
        }
        scope = new MySegmentMark(new PartialMark(key, position));
        return scope;
    }


    @Override
    public synchronized List<StreamSegment> dump() {
        return ImmutableList.copyOf(completedMarks);
    }

    static class StreamMark implements StreamSegment {
        private final String label;
        private final long start, end;

        StreamMark(String label, long start, long end) {
            this.label = label;
            this.start = start;
            this.end = end;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public long start() {
            return start;
        }

        @Override
        public long end() {
            return end;
        }
    }

}
