package smart.scheduling.graph.dagsp;

import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.Edge;
import smart.scheduling.common.metrics.Metrics;
import smart.scheduling.common.model.WeightModel;
import smart.scheduling.common.util.Preconditions;

import java.util.Arrays;
import java.util.*;

public class DagLongestPath {
    private final DirectedGraph dag; private final List<Integer> topo;
    private final WeightModel wm; private final Metrics m;


    public DagLongestPath(DirectedGraph dag, List<Integer> topo, WeightModel wm, Metrics m) {
        this.dag = dag; this.topo = topo; this.wm = wm; this.m = m;
        Preconditions.check(wm == WeightModel.EDGE, "This impl expects EDGE weights");
    }


    public PathResult singleSource(int s) {
        int n = dag.n();
        double[] dist = new double[n]; Arrays.fill(dist, Double.NEGATIVE_INFINITY);
        int[] parent = new int[n]; Arrays.fill(parent, -1);
        dist[s] = 0.0;
        for (int u : topo) {
            if (Double.isInfinite(-dist[u])) continue; // -inf guard
            for (Edge e : dag.neighbors(u)) {
                m.incRelaxations();
                double nd = dist[u] + e.w; // EDGE model
                if (nd > dist[e.v]) { dist[e.v] = nd; parent[e.v] = u; }
            }
        }
        return new PathResult(dist, parent, s);
    }
}
