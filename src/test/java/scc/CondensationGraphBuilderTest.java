package scc;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.graph.scc.CondensationGraphBuilder;
import smart.scheduling.graph.scc.TarjanSCC;
import smart.scheduling.graph.topo.KahnTopologicalSort;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CondensationGraphBuilderTest {

    @Test
    void condensationSizeEqualsSccCount() {
        DirectedGraph g = new DirectedGraph(6);
// SCC A: 0<->1; SCC B: 2<->3; singles: 4,5
        g.addEdge(0,1,1); g.addEdge(1,0,1);
        g.addEdge(2,3,1); g.addEdge(3,2,1);
        g.addEdge(1,2,1); g.addEdge(3,4,1); g.addEdge(4,5,1);


        var scc = new TarjanSCC(g, new smart.scheduling.common.metrics.DefaultMetrics()).compute();
        var dag = CondensationGraphBuilder.build(g, scc);
        assertEquals(scc.components.size(), dag.n());
    }

    @Test
    void condensationHasNoSelfLoopsAndIsDAG() {
        DirectedGraph g = new DirectedGraph(6);
// SCC A: 0<->1
        g.addEdge(0,1,1); g.addEdge(1,0,1);
// SCC B: 2<->3
        g.addEdge(2,3,1); g.addEdge(3,2,1);
// singles: 4,5
        g.addEdge(1,2,1); // A -> B
        g.addEdge(3,4,1); // B -> 4
        g.addEdge(4,5,1); // 4 -> 5


        var r = new TarjanSCC(g, new DefaultMetrics()).compute();
        DirectedGraph dag = CondensationGraphBuilder.build(g, r);


// нет петель
        for (int u = 0; u < dag.n(); u++)
            for (var e : dag.neighbors(u)) assertNotEquals(u, e.v);


// топосорт не падает => DAG
        var order = new KahnTopologicalSort().order(dag, new DefaultMetrics());
        assertEquals(dag.n(), order.size());


// рёбра уникальные (проверим на дубликаты)
        Set<Long> seen = new HashSet<>();
        for (int u = 0; u < dag.n(); u++)
            for (var e : dag.neighbors(u)) {
                long k = (((long)u) << 32) ^ (e.v & 0xffffffffL);
                assertTrue(seen.add(k), "Duplicate edge in condensation");
            }
    }
}