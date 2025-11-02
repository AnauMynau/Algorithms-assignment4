package scc;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.graph.scc.SCCResult;
import smart.scheduling.graph.scc.TarjanSCC;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

public class TarjanSCCTest {


    @Test
    void singleVertex_noEdges_isOneComponent() {
        DirectedGraph g = new DirectedGraph(1);
        TarjanSCC algo = new TarjanSCC(g, new DefaultMetrics());
        SCCResult r = algo.compute();
        assertEquals(1, r.components.size());
        assertEquals(List.of(List.of(0)), r.components);
        assertArrayEquals(new int[]{0}, r.compIdByVertex);
    }


    @Test
    void threeCycle_isSingleSCC() {
        DirectedGraph g = new DirectedGraph(3);
        g.addEdge(0,1,1); g.addEdge(1,2,1); g.addEdge(2,0,1);
        SCCResult r = new TarjanSCC(g, new DefaultMetrics()).compute();
        assertEquals(1, r.components.size());
        assertEquals(3, r.components.get(0).size());
// все вершины в одном комп-ид
        int c = r.compIdByVertex[0];
        assertEquals(c, r.compIdByVertex[1]);
        assertEquals(c, r.compIdByVertex[2]);
    }


    @Test
    void mixedGraph_multipleSCCs_detected() {
        DirectedGraph g = new DirectedGraph(8);
// SCC1: 0->1->2->0
        g.addEdge(0,1,1); g.addEdge(1,2,1); g.addEdge(2,0,1);
// SCC2: 3->4->3
        g.addEdge(3,4,1); g.addEdge(4,3,1);
// chain: 5->6->7 (DAG part)
        g.addEdge(5,6,1); g.addEdge(6,7,1);
// cross edges
        g.addEdge(2,3,1); // from SCC1 to SCC2
        g.addEdge(4,5,1); // from SCC2 to chain


        SCCResult r = new TarjanSCC(g, new DefaultMetrics()).compute();
        assertEquals(5, r.components.size());
        var sizes = r.components.stream().map(List::size).sorted().collect(Collectors.toList());
        assertEquals(List.of(1,1,1,2,3), sizes);



// вершины 0,1,2 в одном компе
        assertEquals(r.compIdByVertex[0], r.compIdByVertex[1]);
        assertEquals(r.compIdByVertex[1], r.compIdByVertex[2]);
// вершины 3,4 в одном компе
        assertEquals(r.compIdByVertex[3], r.compIdByVertex[4]);
// вершины 5,6,7 — все разные компоненты (линейная цепочка)
        assertNotEquals(r.compIdByVertex[5], r.compIdByVertex[6]);
        assertNotEquals(r.compIdByVertex[6], r.compIdByVertex[7]);
    }
}
