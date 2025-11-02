package smart.scheduling.graph.topo;

import smart.scheduling.common.metrics.Metrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.Graph;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.*;

public class KahnTopologicalSort implements TopologicalSort {
    @Override
    public List<Integer> order(Graph g0, Metrics m) {
        DirectedGraph g = (DirectedGraph) g0; // condensation is DirectedGraph
        int n = g.n(); int[] indeg = g.indegrees();
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) { q.add(i); m.incQueuePushes(); }
        List<Integer> out = new ArrayList<>(n);
        while (!q.isEmpty()) {
            int u = q.remove(); m.incQueuePops();
            out.add(u);
            for (var e : g.neighbors(u)) {
                if (--indeg[e.v] == 0) { q.add(e.v); m.incQueuePushes(); }
            }
        }
        if (out.size() != n) throw new IllegalStateException("Graph is not a DAG");
        return out;
    }
}