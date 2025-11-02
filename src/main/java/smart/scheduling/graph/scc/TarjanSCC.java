package smart.scheduling.graph.scc;

import smart.scheduling.common.model.Graph;
import smart.scheduling.common.metrics.Metrics;

import java.util.*;

public class TarjanSCC {
    private final Graph g; private final Metrics m;
    private int time = 0, compCount = 0;
    private int[] disc, low, compId; private boolean[] inStack; private Deque<Integer> st;


    public TarjanSCC(Graph g, Metrics m) { this.g = g; this.m = m; }


    public SCCResult compute() {
        int n = g.n();
        disc = new int[n]; Arrays.fill(disc, -1);
        low = new int[n]; compId = new int[n]; Arrays.fill(compId, -1);
        inStack = new boolean[n]; st = new ArrayDeque<>();
        for (int u = 0; u < n; u++) if (disc[u] == -1) dfs(u);
        List<List<Integer>> comps = new ArrayList<>(compCount);
        for (int i = 0; i < compCount; i++) comps.add(new ArrayList<>());
        for (int v = 0; v < n; v++) comps.get(compId[v]).add(v);
        return new SCCResult(comps, compId);
    }


    private void dfs(int u) {
        m.incDfsVisits();
        disc[u] = low[u] = time++;
        st.push(u); inStack[u] = true;
        for (var e : g.neighbors(u)) {
            m.incEdgeScans();
            int v = e.v;
            if (disc[v] == -1) { dfs(v); low[u] = Math.min(low[u], low[v]); }
            else if (inStack[v]) low[u] = Math.min(low[u], disc[v]);
        }
        if (low[u] == disc[u]) {
            while (true) {
                int v = st.pop(); inStack[v] = false; compId[v] = compCount;
                if (v == u) break;
            }
            compCount++;
        }
    }
}
