package metrics;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.graph.topo.KahnTopologicalSort;


import static org.junit.jupiter.api.Assertions.*;


public class MetricsSanityTest {


    @Test
    void countersAreNonNegative_afterTopo() {
        DirectedGraph dag = new DirectedGraph(4);
        dag.addEdge(0,1,1); dag.addEdge(1,2,1); dag.addEdge(2,3,1);
        var m = new DefaultMetrics();
        new KahnTopologicalSort().order(dag, m);
        var snap = m.snapshot();
        snap.values().forEach(v -> assertTrue(((Number)v).longValue() >= 0));
    }
}