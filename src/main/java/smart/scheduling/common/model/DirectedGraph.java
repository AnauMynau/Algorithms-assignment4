package smart.scheduling.common.model;

import smart.scheduling.common.util.Preconditions;

import java.util.*;

public class DirectedGraph implements Graph {
    private final int n;
    private final List<List<Edge>> adj;


    public DirectedGraph(int n) {
        this.n = n;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }


    public int n() { return n; }

    public int m() {
        int count = 0;
        for (int u = 0; u < n; u++) {
            count += adj.get(u).size();
        }
        return count;
    }


    public void addEdge(int u, int v, double w) {
        Preconditions.checkIndex(u, n); Preconditions.checkIndex(v, n);
        adj.get(u).add(new Edge(u, v, w));
    }


    public List<Edge> neighbors(int u) { return adj.get(u); }


    public int[] indegrees() {
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) for (Edge e : adj.get(u)) indeg[e.v]++;
        return indeg;
    }
}
