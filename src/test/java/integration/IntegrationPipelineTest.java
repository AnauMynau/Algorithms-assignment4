package integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import smart.scheduling.common.io.GraphIO;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.WeightModel;
import smart.scheduling.graph.dagsp.DagLongestPath;
import smart.scheduling.graph.dagsp.DagShortestPath;
import smart.scheduling.graph.scc.CondensationGraphBuilder;
import smart.scheduling.graph.scc.TarjanSCC;
import smart.scheduling.graph.topo.KahnTopologicalSort;

import java.nio.file.*;


import static org.junit.jupiter.api.Assertions.*;


public class IntegrationPipelineTest {


    @Test
    void pipeline_runs_on_small2_ifPresent() throws Exception {
        Path p = Path.of("data/small_2_dag.json");
        Assumptions.assumeTrue(Files.exists(p), "test data/small_2_dag.json not found, skipping");


        GraphIO.InputData d = GraphIO.readInput(p);
        DirectedGraph g = GraphIO.toGraph(d);
        var m = new DefaultMetrics();
        var scc = new TarjanSCC(g, m).compute();
        var dag = CondensationGraphBuilder.build(g, scc);
        var topo = new KahnTopologicalSort().order(dag, m);
        var sp = new DagShortestPath(dag, topo, WeightModel.EDGE, m).singleSource(d.source);
        var lp = new DagLongestPath(dag, topo, WeightModel.EDGE, m).singleSource(d.source);


        assertEquals(dag.n(), topo.size());
        for (int v = 0; v < dag.n(); v++) {
            if (Double.isInfinite(sp.dist[v]) || Double.isInfinite(-lp.dist[v])) continue;
            assertTrue(lp.dist[v] >= sp.dist[v] - 1e-9);
        }
    }
}