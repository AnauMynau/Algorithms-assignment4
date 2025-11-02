package smart.scheduling.graph.dagsp;

import java.util.*;

public final class PathResult {
    public final double[] dist; // distances from source
    public final int[] parent; // parent to reconstruct path
    public final int source;


    public PathResult(double[] dist, int[] parent, int source) {
        this.dist = dist; this.parent = parent; this.source = source;
    }


    public List<Integer> pathTo(int t) {
        if (Double.isInfinite(dist[t])) return List.of();
        List<Integer> rev = new ArrayList<>();
        for (int v = t; v != -1; v = parent[v]) rev.add(v);
        Collections.reverse(rev);
        return rev;
    }
}
