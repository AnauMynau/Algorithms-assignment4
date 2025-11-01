package smart.scheduling.app;

import smart.scheduling.common.io.GraphIO;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.metrics.Metrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.WeightModel;
import smart.scheduling.common.util.MetricsExporter;
import smart.scheduling.graph.scc.*;
import smart.scheduling.graph.topo.KahnTopologicalSort;
import smart.scheduling.graph.dagsp.*;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws Exception {
        // === 1. Указываем файл ===
        Path inputPath = Path.of("data/small_2_dag.json"); // можно заменить на любой файл
        GraphIO.InputData input = GraphIO.readInput(inputPath);
        DirectedGraph graph = GraphIO.toGraph(input);

        // === 2. Создаём метрики ===
        Metrics m = new DefaultMetrics();

        // === 3. Выполняем SCC ===
        var scc = new TarjanSCC(graph, m).compute();
        System.out.println("SCC components: " + scc.components.size());

        // === 4. Конденсация в DAG ===
        var dag = CondensationGraphBuilder.build(graph, scc);
        System.out.println("Condensed DAG vertices: " + dag.n());

        // === 5. Топологическая сортировка ===
        var topo = new KahnTopologicalSort().order(dag, m);
        System.out.println("Topological order: " + topo);

        // === 6. Кратчайшие и длиннейшие пути ===
        var sp = new DagShortestPath(dag, topo, WeightModel.EDGE, m).singleSource(input.source);
        var lp = new DagLongestPath(dag, topo, WeightModel.EDGE, m).singleSource(input.source);
        System.out.println("Shortest distances: " + java.util.Arrays.toString(sp.dist));
        System.out.println("Longest distances:  " + java.util.Arrays.toString(lp.dist));

        // === ✅ 7. Экспорт метрик в CSV ===
        MetricsExporter.saveToCsv(m, "results/metrics_scc.csv");

        System.out.println("✅ All computations completed successfully!");
    }
}
