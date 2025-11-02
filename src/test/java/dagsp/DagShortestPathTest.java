package dagsp;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.WeightModel;
import smart.scheduling.graph.dagsp.DagShortestPath;
import smart.scheduling.graph.dagsp.PathResult;


import static org.junit.jupiter.api.Assertions.*;


import java.util.List;


public class DagShortestPathTest {


    private static List<Integer> topoChain(int n) {
        return java.util.stream.IntStream.range(0, n).boxed().toList();
    }


    @Test
    void singleSource_onSimpleDAG_chain() {
        DirectedGraph dag = new DirectedGraph(5);
        dag.addEdge(0,1,2); dag.addEdge(1,2,3); dag.addEdge(2,3,4); dag.addEdge(3,4,5);
        var sp = new DagShortestPath(dag, topoChain(5), WeightModel.EDGE, new DefaultMetrics());
        PathResult r = sp.singleSource(0);
        assertArrayEquals(new double[]{0,2,5,9,14}, r.dist, 1e-9);
        assertEquals(java.util.List.of(0,1,2,3,4), r.pathTo(4));
    }


    @Test
    void branches_chooseCheapest() {
        DirectedGraph dag = new DirectedGraph(4);
        dag.addEdge(0,1,10);
        dag.addEdge(0,2,1);
        dag.addEdge(2,1,1);
        dag.addEdge(1,3,1);
        var topo = java.util.List.of(0,2,1,3);
        var sp = new DagShortestPath(dag, topo, WeightModel.EDGE, new DefaultMetrics());
        PathResult r = sp.singleSource(0);
        assertEquals(2.0, r.dist[1], 1e-9); // 0->2->1 (1+1)
        assertEquals(java.util.List.of(0,2,1,3), r.pathTo(3));
    }
}
