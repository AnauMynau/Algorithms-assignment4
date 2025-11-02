package dagsp;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.WeightModel;
import smart.scheduling.graph.dagsp.DagLongestPath;
import smart.scheduling.graph.dagsp.PathResult;


import static org.junit.jupiter.api.Assertions.*;


import java.util.List;


public class DagLongestPathTest {


    private static List<Integer> topoChain(int n) {
        return java.util.stream.IntStream.range(0, n).boxed().toList();
    }


    @Test
    void longestPath_onChain_equalsSum() {
        DirectedGraph dag = new DirectedGraph(4);
        dag.addEdge(0,1,2); dag.addEdge(1,2,5); dag.addEdge(2,3,7);
        var lp = new DagLongestPath(dag, topoChain(4), WeightModel.EDGE, new DefaultMetrics());
        PathResult r = lp.singleSource(0);
        assertEquals(14.0, r.dist[3], 1e-9);
        assertEquals(java.util.List.of(0,1,2,3), r.pathTo(3));
    }


    @Test
    void longestPath_prefersHeavierBranch() {
        DirectedGraph dag = new DirectedGraph(5);
        dag.addEdge(0,1,1); dag.addEdge(1,4,1);
        dag.addEdge(0,2,3); dag.addEdge(2,3,3); dag.addEdge(3,4,3);
        var topo = java.util.List.of(0,1,2,3,4);
        var lp = new DagLongestPath(dag, topo, WeightModel.EDGE, new DefaultMetrics());
        PathResult r = lp.singleSource(0);
        assertEquals(9.0, r.dist[4], 1e-9);
        assertEquals(java.util.List.of(0,2,3,4), r.pathTo(4));
    }
}