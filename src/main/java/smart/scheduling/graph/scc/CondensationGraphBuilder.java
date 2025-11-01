package smart.scheduling.graph.scc;

import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.Edge;
import smart.scheduling.common.model.Graph;

import java.util.*;

public final class CondensationGraphBuilder {
    public static DirectedGraph build(Graph g, SCCResult scc) {
        int comps = scc.components.size();
        DirectedGraph dag = new DirectedGraph(comps);
        Set<Long> seen = new HashSet<>();
        for (int u = 0; u < g.n(); u++)
            for (Edge e : g.neighbors(u)) {
                int cu = scc.compIdByVertex[e.u];
                int cv = scc.compIdByVertex[e.v];
                if (cu != cv) {
                    long key = (((long)cu) << 32) ^ (cv & 0xffffffffL);
                    if (seen.add(key)) dag.addEdge(cu, cv, e.w);
                }
            }
        return dag;
    }
}
