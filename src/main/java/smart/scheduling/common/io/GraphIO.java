package smart.scheduling.common.io;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import smart.scheduling.common.metrics.Metrics;
import smart.scheduling.common.model.DirectedGraph;
import smart.scheduling.common.model.Edge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class GraphIO {
    private static final ObjectMapper M = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /* ========= ВХОДНОЙ JSON ========= */

    public static class InputData {
        public boolean directed;
        public int n;
        public List<InputEdge> edges;
        public Integer source;          // optional
        public String weight_model;     // "edge" | "node" (может быть неиспользовано — ок)
    }
    public static class InputEdge { public int u, v; public double w; }

    public static InputData readInput(Path p) throws IOException {
        try (var in = Files.newInputStream(p)) {
            return M.readValue(in, InputData.class);
        }
    }

    public static DirectedGraph toGraph(InputData d) {
        if (!d.directed) throw new IllegalArgumentException("Input must be directed");
        DirectedGraph g = new DirectedGraph(d.n);
        if (d.edges != null) {
            for (InputEdge e : d.edges) g.addEdge(e.u, e.v, e.w);
        }
        return g;
    }

    /* ========= РЕНДЕР РЕЗУЛЬТАТОВ ========= */

    public static String toSccJson(Object scc, DirectedGraph dag) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scc", scc);
        out.put("condensation", serializeGraph(dag));
        return toJson(out);
    }

    public static String toTopoJson(Object scc, DirectedGraph dag, List<Integer> order, Metrics m) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scc", scc);
        out.put("condensation", serializeGraph(dag));
        out.put("topo_order", order);
        out.put("metrics", m.snapshot());
        return toJson(out);
    }

    public static String toDagSpJson(String mode, int source, Object r, Metrics m) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", mode);
        out.put("source", source);
        out.put("result", r);
        out.put("metrics", m.snapshot());
        return toJson(out);
    }

    public static String toAllJson(Object scc, DirectedGraph dag, List<Integer> order,
                                   Object shortest, Object longest, Metrics m) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("scc", scc);
        out.put("condensation", serializeGraph(dag));
        out.put("topo_order", order);
        out.put("shortest", shortest);
        out.put("longest", longest);
        out.put("metrics", m.snapshot());
        return toJson(out);
    }

    /* ========= ХЕЛПЕРЫ ========= */

    private static Map<String, Object> serializeGraph(DirectedGraph g) {
        Map<String, Object> x = new LinkedHashMap<>();
        x.put("n", g.n());
        List<Map<String, Object>> es = new ArrayList<>();
        for (int u = 0; u < g.n(); u++) {
            for (Edge e : g.neighbors(u)) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("u", e.u);
                m.put("v", e.v);
                m.put("w", e.w);
                es.add(m);
            }
        }
        x.put("edges", es);
        return x;
    }

    private static String toJson(Object o) {
        try { return M.writeValueAsString(o); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
}
