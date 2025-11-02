package dagsp;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.WeightModel;
import smart.scheduling.graph.dagsp.DagLongestPath;
import smart.scheduling.graph.dagsp.DagShortestPath;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

public class DagSPUnreachableAndCriticalTest {
    private static List<Integer> topoChain(int n) {
        return java.util.stream.IntStream.range(0, n).boxed().toList();
    }


    @Test
    void unreachableVertices_infiniteDistance() {
// 0->1, 2 изолирована
        DirectedGraph dag = new DirectedGraph(3);
        dag.addEdge(0,1,1);
        var sp = new DagShortestPath(dag, topoChain(3), WeightModel.EDGE, new DefaultMetrics());
        var r = sp.singleSource(0);
        assertTrue(Double.isInfinite(r.dist[2]));
        assertEquals(List.of(), r.pathTo(2));
    }


    @Test
    void criticalPath_onForkJoin() {
        DirectedGraph dag = new DirectedGraph(6);
// 0 -> {1(w=2),2(w=5)}; 1->3(w=5); 2->4(w=1); {3,4}->5
        dag.addEdge(0,1,2); dag.addEdge(0,2,5);
        dag.addEdge(1,3,5); dag.addEdge(2,4,1);
        dag.addEdge(3,5,1); dag.addEdge(4,5,1);
        var topo = List.of(0,1,2,3,4,5);


        var lp = new DagLongestPath(dag, topo, WeightModel.EDGE, new DefaultMetrics()).singleSource(0);
        assertEquals(8.0, lp.dist[5], 1e-9); // 0->1(2)->3(5)->5(1)
        assertEquals(List.of(0,1,3,5), lp.pathTo(5));


        var sp = new DagShortestPath(dag, topo, WeightModel.EDGE, new DefaultMetrics()).singleSource(0);
        assertTrue(lp.dist[5] >= sp.dist[5] - 1e-9);
    }
}

