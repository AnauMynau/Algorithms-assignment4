package smart.scheduling.app;

import smart.scheduling.common.io.GraphIO;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.metrics.Metrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.WeightModel;
import smart.scheduling.graph.scc.CondensationGraphBuilder;
import smart.scheduling.graph.scc.SCCResult;
import smart.scheduling.graph.scc.TarjanSCC;
import smart.scheduling.graph.topo.KahnTopologicalSort;
import smart.scheduling.graph.topo.TopologicalSort;
import smart.scheduling.graph.dagsp.DagShortestPath;
import smart.scheduling.graph.dagsp.DagLongestPath;
import smart.scheduling.common.util.SummaryCsvLite;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        List<Path> files = resolveInputs(args);
        if (files.isEmpty()) {
            files.addAll(glob("data/small_*.json"));
            files.addAll(glob("data/medium_*.json"));
            files.addAll(glob("data/large_*.json"));
        }

        SummaryCsvLite lite = new SummaryCsvLite("results/summary_metrics_lite.csv");

        for (Path inputPath : files) {
            System.out.println("📊 Processing: " + inputPath);
            runOnce(inputPath, lite);
        }
        System.out.println("📈 Saved to results/summary_metrics_lite.csv");
    }

    private static void runOnce(Path inputPath, SummaryCsvLite lite) throws Exception {
        GraphIO.InputData input = GraphIO.readInput(inputPath);
        DirectedGraph graph = GraphIO.toGraph(input);
        Metrics m = new DefaultMetrics();

        // --- SCC ---
        long t0 = System.nanoTime();
        SCCResult scc = new TarjanSCC(graph, m).compute();
        long t1 = System.nanoTime();
        double density = SummaryCsvLite.densityDirected(graph.n(), graph.m());
        lite.writeRow(
                inputPath.getFileName().toString(), "SCC",
                graph.n(), graph.m(), scc.components.size(), null,
                null, null,
                SummaryCsvLite.millisFromNanos(t0, t1), density,
                graph.m() == 0 ? null : SummaryCsvLite.millisFromNanos(t0, t1) / (double) graph.m(),
                "Tarjan SCC"
        );

        // --- Condensation + Topo ---
        t0 = System.nanoTime();
        DirectedGraph dag = CondensationGraphBuilder.build(graph, scc);
        TopologicalSort topoAlg = new KahnTopologicalSort();
        List<Integer> topo = topoAlg.order(dag, m);
        t1 = System.nanoTime();
        double dagDensity = SummaryCsvLite.densityDirected(dag.n(), dag.m());
        lite.writeRow(
                inputPath.getFileName().toString(), "Topo",
                dag.n(), dag.m(), scc.components.size(), topo.size(),
                null, null,
                SummaryCsvLite.millisFromNanos(t0, t1), dagDensity,
                dag.m() == 0 ? null : SummaryCsvLite.millisFromNanos(t0, t1) / (double) dag.m(),
                "Condensation + Kahn"
        );

        // --- source vertex -> SCC component index ---
        int srcVertex = (input.source == null ? 0 : input.source);
        if (srcVertex < 0 || srcVertex >= graph.n()) srcVertex = 0;
        int srcComp = scc.compIdByVertex[srcVertex];

        // --- Shortest ---
        t0 = System.nanoTime();
        var sp = new DagShortestPath(dag, topo, WeightModel.EDGE, m).singleSource(srcComp);
        t1 = System.nanoTime();
        lite.writeRow(
                inputPath.getFileName().toString(), "Shortest",
                dag.n(), dag.m(), scc.components.size(), topo.size(),
                null, SummaryCsvLite.avgFinite(sp.dist),
                SummaryCsvLite.millisFromNanos(t0, t1), dagDensity,
                dag.m() == 0 ? null : SummaryCsvLite.millisFromNanos(t0, t1) / (double) dag.m(),
                "DAG shortest paths"
        );

        // --- Longest (critical path) ---
        t0 = System.nanoTime();
        var lp = new DagLongestPath(dag, topo, WeightModel.EDGE, m).singleSource(srcComp);
        t1 = System.nanoTime();
        lite.writeRow(
                inputPath.getFileName().toString(), "Longest",
                dag.n(), dag.m(), scc.components.size(), topo.size(),
                SummaryCsvLite.maxFinite(lp.dist), SummaryCsvLite.avgFinite(lp.dist),
                SummaryCsvLite.millisFromNanos(t0, t1), dagDensity,
                dag.m() == 0 ? null : SummaryCsvLite.millisFromNanos(t0, t1) / (double) dag.m(),
                "Critical path"
        );
    }

    // ---------- helpers (Windows-safe glob) ----------
    private static List<Path> glob(String pattern) throws IOException {
        String p = pattern.replace('\\', '/');
        int slash = p.lastIndexOf('/');
        String dirStr = (slash >= 0) ? p.substring(0, slash) : ".";
        String mask   = (slash >= 0) ? p.substring(slash + 1) : p;

        Path dir = Path.of(dirStr);
        List<Path> out = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, mask)) {
            for (Path file : ds) out.add(file);
        }
        out.sort(null);
        return out;
    }

    private static List<Path> resolveInputs(String[] args) throws IOException {
        List<Path> out = new ArrayList<>();
        if (args == null || args.length == 0) return out;

        if (args.length >= 1 && "--all".equalsIgnoreCase(args[0])) {
            out.addAll(glob("data/*.json"));
            return out;
        }
        if (args.length >= 2 && "--glob".equalsIgnoreCase(args[0])) {
            out.addAll(glob(args[1]));
            return out;
        }
        for (String a : args) out.add(Path.of(a));
        return out;
    }
}
