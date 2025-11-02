package smart.scheduling.graph.topo;

import smart.scheduling.common.metrics.Metrics;
import smart.scheduling.common.model.Graph;

import java.util.*;

public interface TopologicalSort {
    List<Integer> order(Graph dag, Metrics m);
}
