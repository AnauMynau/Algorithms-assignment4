package smart.scheduling.common.util;

public final class Stopwatch implements AutoCloseable {
    private final long start;
    private final java.util.function.LongConsumer sink;


    public Stopwatch(java.util.function.LongConsumer sink) {
        this.start = System.nanoTime();
        this.sink = sink;
    }


    @Override public void close() { sink.accept(System.nanoTime() - start); }
}