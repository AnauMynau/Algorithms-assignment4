package smart.scheduling.app;

import smart.scheduling.common.io.GraphIO;
import smart.scheduling.common.metrics.DefaultMetrics;
import smart.scheduling.common.metrics.Metrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.WeightModel;
import smart.scheduling.common.util.MetricsExporter;
import smart.scheduling.common.util.Preconditions;
import smart.scheduling.graph.dagsp.DagLongestPath;
import smart.scheduling.graph.dagsp.DagShortestPath;
import smart.scheduling.graph.dagsp.PathResult;
import smart.scheduling.graph.scc.CondensationGraphBuilder;
import smart.scheduling.graph.scc.SCCResult;
import smart.scheduling.graph.scc.TarjanSCC;
import smart.scheduling.graph.topo.KahnTopologicalSort;
import smart.scheduling.graph.topo.TopologicalSort;

import java.util.*;
import java.nio.file.Path;

public class Cli {
    private static final String USAGE = String.join(System.lineSeparator(),
            "Usage:",
            " scc --in <file.json>",
            " topo --in <file.json>",
            " dagsp --in <file.json> --source <s> --mode shortest|longest",
            " all --in <file.json> [--source <s>]",
            "Examples:",
            " java -jar app.jar scc --in data/tasks.json",
            " java -jar app.jar dagsp --in data/tasks.json --source 4 --mode shortest"
    );

    public int run(String[] args) {
        if (args.length == 0) { System.out.println(USAGE); return 1; }
        Map<String, String> kv = parse(args);
        String cmd = args[0];
        try {
            switch (cmd) {
                case "scc":
                    return cmdScc(kv);
                case "topo":
                    return cmdTopo(kv);
                case "dagsp":
                    return cmdDagSp(kv);
                case "all":
                    return cmdAll(kv);
                default:
                    System.out.println(USAGE); return 2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 3;
        }
    }

    private int cmdScc(Map<String, String> kv) throws Exception {
        Path in = Path.of(req(kv, "--in"));
        GraphIO.InputData data = GraphIO.readInput(in);
        DirectedGraph g = GraphIO.toGraph(data);
        Metrics m = new DefaultMetrics();
        TarjanSCC tarjan = new TarjanSCC(g, m);
        SCCResult scc = tarjan.compute();
        var dag = CondensationGraphBuilder.build(g, scc);
        System.out.println(GraphIO.toSccJson(scc, dag));
        return 0;
    }

    private int cmdTopo(Map<String, String> kv) throws Exception {
        Path in = Path.of(req(kv, "--in"));
        GraphIO.InputData data = GraphIO.readInput(in);
        DirectedGraph g = GraphIO.toGraph(data);
        Metrics m = new DefaultMetrics();
        TarjanSCC tarjan = new TarjanSCC(g, m);
        SCCResult scc = tarjan.compute();
        var dag = CondensationGraphBuilder.build(g, scc);
        TopologicalSort topo = new KahnTopologicalSort();
        List<Integer> order = topo.order(dag, m);
        System.out.println(GraphIO.toTopoJson(scc, dag, order, m));
        return 0;
    }

    private int cmdDagSp(Map<String, String> kv) throws Exception {
        Path in = Path.of(req(kv, "--in"));
        String mode = req(kv, "--mode");
        Integer source = optInt(kv.get("--source"));
        GraphIO.InputData data = GraphIO.readInput(in);
        DirectedGraph g = GraphIO.toGraph(data);
        int s = (source != null) ? source : Preconditions.checkNotNull(data.source, "source not provided in args or JSON");


        Metrics m = new DefaultMetrics();
            // Compute condensation + topo
        TarjanSCC tarjan = new TarjanSCC(g, m);
        SCCResult scc = tarjan.compute();
        var dag = CondensationGraphBuilder.build(g, scc);
        TopologicalSort topo = new KahnTopologicalSort();
        List<Integer> order = topo.order(dag, m);


        if ("shortest".equalsIgnoreCase(mode)) {
            DagShortestPath sp = new DagShortestPath(dag, order, WeightModel.EDGE, m);
            PathResult r = sp.singleSource(s);
            System.out.println(GraphIO.toDagSpJson("shortest", s, r, m));
        } else if ("longest".equalsIgnoreCase(mode)) {
            DagLongestPath lp = new DagLongestPath(dag, order, WeightModel.EDGE, m);
            PathResult r = lp.singleSource(s);
            System.out.println(GraphIO.toDagSpJson("longest", s, r, m));
        } else {
            throw new IllegalArgumentException("--mode shortest|longest");
        }
        return 0;
    }

    private int cmdAll(Map<String, String> kv) throws Exception {
        Path in = Path.of(req(kv, "--in"));
        Integer source = optInt(kv.get("--source"));
        GraphIO.InputData data = GraphIO.readInput(in);
        DirectedGraph g = GraphIO.toGraph(data);
        int s = (source != null) ? source : Preconditions.checkNotNull(data.source, "source not provided in args or JSON");


        Metrics m = new DefaultMetrics();
        TarjanSCC tarjan = new TarjanSCC(g, m);
        SCCResult scc = tarjan.compute();
        var dag = CondensationGraphBuilder.build(g, scc);
        TopologicalSort topo = new KahnTopologicalSort();
        List<Integer> order = topo.order(dag, m);


        DagShortestPath sp = new DagShortestPath(dag, order, WeightModel.EDGE, m);
        PathResult shortest = sp.singleSource(s);
        DagLongestPath lp = new DagLongestPath(dag, order, WeightModel.EDGE, m);
        PathResult longest = lp.singleSource(s);


        System.out.println(GraphIO.toAllJson(scc, dag, order, shortest, longest, m));
        return 0;
    }


    private static String req(Map<String, String> kv, String k) {
        String v = kv.get(k);
        if (v == null) throw new IllegalArgumentException("Missing " + k);
        return v;
    }


    private static Integer optInt(String s) {
        if (s == null) return null; return Integer.parseInt(s);
    }


    private static Map<String, String> parse(String[] args) {
        Map<String, String> kv = new HashMap<>();
        if (args.length == 0) return kv;
        kv.put("__cmd", args[0]);
        for (int i = 1; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("--")) {
                String key = a;
                String val = (i + 1 < args.length && !args[i+1].startsWith("--")) ? args[++i] : "true";
                kv.put(key, val);
            }
        }
        return kv;
    }
}
