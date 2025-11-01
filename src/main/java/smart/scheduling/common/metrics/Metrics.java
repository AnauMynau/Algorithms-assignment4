package smart.scheduling.common.metrics;

import java.util.*;

public interface Metrics {
    void incDfsVisits();
    void incEdgeScans();
    void incQueuePushes();
    void incQueuePops();
    void incRelaxations();


    void addTimeNanos(long nanos);


    Map<String, Object> snapshot();
}