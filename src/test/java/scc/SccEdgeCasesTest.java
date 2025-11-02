package scc;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.graph.scc.SCCResult;
import smart.scheduling.graph.scc.TarjanSCC;

import static org.junit.jupiter.api.Assertions.*;

public class SccEdgeCasesTest {
    @Test
    void selfLoop_isSingleScc() {
        DirectedGraph g = new DirectedGraph(3);
        g.addEdge(0,0,1); // петля
        TarjanSCC t = new TarjanSCC(g, new DefaultMetrics());
        SCCResult r = t.compute();
// {0} — отдельная компонента, 1 и 2 — по одиночке
        assertEquals(3, r.components.size());
        assertNotEquals(r.compIdByVertex[0], r.compIdByVertex[1]);
        assertNotEquals(r.compIdByVertex[1], r.compIdByVertex[2]);
    }


    @Test
    void multipleParallelEdges_doNotAffectScc() {
        DirectedGraph g = new DirectedGraph(2);
        g.addEdge(0,1,1);
        g.addEdge(0,1,2);
        g.addEdge(1,0,3);
        TarjanSCC t = new TarjanSCC(g, new DefaultMetrics());
        SCCResult r = t.compute();
// оба узла в одном SCC
        assertEquals(r.compIdByVertex[0], r.compIdByVertex[1]);
        assertEquals(1, r.components.size());
    }


    @Test
    void fullyDisconnectedGraph_eachIsOwnScc() {
        DirectedGraph g = new DirectedGraph(5);
        SCCResult r = new TarjanSCC(g, new DefaultMetrics()).compute();
        assertEquals(5, r.components.size());
        int c0 = r.compIdByVertex[0];
        for (int i = 1; i < 5; i++) assertNotEquals(c0, r.compIdByVertex[i]);
    }
}

