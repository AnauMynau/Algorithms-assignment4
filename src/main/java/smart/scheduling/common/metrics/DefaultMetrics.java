package smart.scheduling.common.metrics;

import java.util.*;

public class DefaultMetrics implements Metrics {
    private long dfsVisits, edgeScans, queuePushes, queuePops, relaxations, timeNanos;


    public void incDfsVisits() { dfsVisits++; }
    public void incEdgeScans() { edgeScans++; }
    public void incQueuePushes() { queuePushes++; }
    public void incQueuePops() { queuePops++; }
    public void incRelaxations() { relaxations++; }


    public void addTimeNanos(long nanos) { timeNanos += nanos; }


    public Map<String, Object> snapshot() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("dfsVisits", dfsVisits);
        m.put("edgeScans", edgeScans);
        m.put("queuePushes", queuePushes);
        m.put("queuePops", queuePops);
        m.put("relaxations", relaxations);
        m.put("timeNanos", timeNanos);
        return m;
    }
}