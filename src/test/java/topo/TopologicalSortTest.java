package topo;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.graph.topo.KahnTopologicalSort;

import static org.junit.jupiter.api.Assertions.*;


public class TopologicalSortTest {

    @Test
    void diamondDag_respectsInvariant() {
        DirectedGraph dag = new DirectedGraph(4);
        dag.addEdge(0,1,1); dag.addEdge(0,2,1); dag.addEdge(1,3,1); dag.addEdge(2,3,1);
        var ord = new KahnTopologicalSort().order(dag, new DefaultMetrics());
        int[] pos = new int[4];
        for (int i = 0; i < ord.size(); i++) pos[ord.get(i)] = i;
        for (int u = 0; u < 4; u++) for (var e : dag.neighbors(u)) assertTrue(pos[u] < pos[e.v]);
    }

    @Test
    void multiSources_multiSinks_ok() {
        DirectedGraph dag = new DirectedGraph(6);
        dag.addEdge(0,3,1); dag.addEdge(1,3,1); dag.addEdge(2,4,1); dag.addEdge(3,5,1); dag.addEdge(4,5,1);
        var ord = new KahnTopologicalSort().order(dag, new DefaultMetrics());
        assertEquals(6, ord.size());
    }


    @Test
    void kahnProducesValidOrderOnDAG() {
        DirectedGraph dag = new DirectedGraph(6);
        dag.addEdge(0,1,1); dag.addEdge(0,2,1);
        dag.addEdge(1,3,1); dag.addEdge(2,3,1);
        dag.addEdge(3,4,1); dag.addEdge(4,5,1);


        var order = new KahnTopologicalSort().order(dag, new DefaultMetrics());
        assertEquals(6, order.size());


// проверка инварианта: для каждого ребра u->v позиция(u) < позиция(v)
        int[] pos = new int[6];
        for (int i = 0; i < order.size(); i++) pos[order.get(i)] = i;
        for (int u = 0; u < 6; u++)
            for (var e : dag.neighbors(u)) assertTrue(pos[u] < pos[e.v]);
    }


    @Test
    void kahnThrowsIfNotDAG() {
        DirectedGraph g = new DirectedGraph(3);
        g.addEdge(0,1,1); g.addEdge(1,2,1); g.addEdge(2,0,1); // цикл
        assertThrows(IllegalStateException.class,
                () -> new KahnTopologicalSort().order(g, new DefaultMetrics()));
    }
}
