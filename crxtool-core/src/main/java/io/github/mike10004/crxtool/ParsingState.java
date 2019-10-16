package io.github.mike10004.crxtool;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

interface ParsingState {

    void markStart(String key);

    String markEnd();

    default void markEndChecked(String key) {
        String currentLabel = markEnd();
        if (!Objects.equals(key, currentLabel)) {
            throw new IllegalArgumentException("end label does not match start label: end " + key + " but started " + currentLabel);
        }
    }


    interface Mark {
        String label();
        long start();
        long end();
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

    private PartialMark current;
    private final LongSupplier positionGetter;
    private final List<Mark> completedMarks = new ArrayList<>();

    public StreamParsingState(LongSupplier positionGetter) {
        this.positionGetter = positionGetter;
    }

    @Override
    public synchronized void markStart(String key) {
        long position = positionGetter.getAsLong();
        if (current != null) {
            completeCurrent(position);
        }
        current = new PartialMark(key, position);
    }

    private Mark completeCurrent(long position) {
        Mark completed = current.complete(position);
        completedMarks.add(completed);
        current = null;
        return completed;
    }

    @Override
    public synchronized String markEnd() {
        Mark completed = completeCurrent(positionGetter.getAsLong());
        return completed.label();
    }

    @Override
    public synchronized List<Mark> dump() {
        return ImmutableList.copyOf(completedMarks);
    }
}
