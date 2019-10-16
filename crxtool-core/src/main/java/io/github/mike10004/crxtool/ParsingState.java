package io.github.mike10004.crxtool;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CountingInputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.LongSupplier;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

interface ParsingState {

    MarkScope markStart(String key);

    Mark markEnd(String key);

    interface Mark {
        String label();
        long start();
        long end();
        default long length() {
            return end() - start();
        }
    }

    List<Mark> dump();
}

class StreamMark implements ParsingState.Mark {
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

interface MarkScope extends AutoCloseable {
    @Override
    void close();
}

class PartialMark {

    public final String label;
    public final long start;

    PartialMark(String label, long start) {
        this.label = label;
        this.start = start;
    }

    public ParsingState.Mark complete(long end) {
        return new StreamMark(label, start, end);
    }
}

class StreamParsingState implements ParsingState {

    private volatile MyMarkScope scope;
    private final LongSupplier positionGetter;
    private final List<Mark> completedMarks = new ArrayList<>();

    public StreamParsingState(LongSupplier positionGetter) {
        this.positionGetter = requireNonNull(positionGetter);
    }

    public static StreamParsingState fromStream(CountingInputStream in) {
        return new StreamParsingState(in::getCount);
    }

    private class MyMarkScope implements MarkScope {

        private final PartialMark partial;

        private MyMarkScope(PartialMark partial) {
            this.partial = partial;
        }

        @Override
        public final void close() {
            closeAndReturnCompletedMark();
        }

        public final Mark closeAndReturnCompletedMark() {
            checkState(this == scope, "this is not the current scope");
            scope = null;
            long position = positionGetter.getAsLong();
            Mark completed = partial.complete(position);
            completedMarks.add(completed);
            return completed;
        }
    }

    @Override
    public synchronized MarkScope markStart(String key) {
        long position = positionGetter.getAsLong();
        if (scope != null) {
            scope.close();
        }
        scope = new MyMarkScope(new PartialMark(key, position));
        return scope;
    }


    @Override
    public synchronized Mark markEnd(String label) {
        checkState(scope != null, "no mark started");
        Mark mark = scope.closeAndReturnCompletedMark();
        if (!Objects.equals(label, mark.label())) {
            throw new IllegalStateException(String.format("end label does not match start label: end %s != %s start", label, mark.label()));
        }
        return mark;
    }

    @Override
    public synchronized List<Mark> dump() {
        return ImmutableList.copyOf(completedMarks);
    }
}
