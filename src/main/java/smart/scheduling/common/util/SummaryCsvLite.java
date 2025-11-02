package smart.scheduling.common.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.UncheckedIOException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SummaryCsvLite {
    private final Path path;
    private boolean headerWritten;

    public SummaryCsvLite(String file) {
        this.path = Path.of(file);
        this.headerWritten = Files.exists(this.path);
    }

    private static String esc(Object x) {
        if (x == null) return "";
        String s = String.valueOf(x);
        if (s.contains(",") || s.contains("\"")) s = "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
    private static void ensureParent(Path p) {
        try { Files.createDirectories(p.getParent()); } catch (IOException ignored) {}
    }

    // Упрощённая запись строки
    public void writeRow(
            String graph, String stage,
            Integer n, Integer edges,
            Integer sccCount, Integer topoLength,
            Double criticalPathLen, Double avgPathLen,
            Double timeMillis, Double density, Double speedPerEdge,
            String notes
    ) {
        try {
            ensureParent(path);
            try (var out = new BufferedWriter(new FileWriter(path.toFile(), true))) {
                if (!headerWritten) {
                    out.write(String.join(",", "graph","stage","n","edges","sccCount","topoLength",
                            "criticalPathLen","avgPathLen","timeMillis","density","speedPerEdge","notes"));
                    out.write("\n");
                    headerWritten = true;
                }
                out.write(String.join(",",
                        esc(graph), esc(stage), esc(n), esc(edges), esc(sccCount), esc(topoLength),
                        esc(criticalPathLen), esc(avgPathLen), esc(timeMillis), esc(density), esc(speedPerEdge), esc(notes)
                ));
                out.write("\n");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Полезные хелперы
    public static double densityDirected(int n, int edges) {
        if (n <= 1) return 0.0;
        return edges / (double)(n * (long)(n - 1));
    }


    public static long usedMemoryMB() {
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        return Math.max(used, 0);
    }
    public static Double avgFinite(double[] dist) {
        long cnt = 0; double sum = 0;
        for (double d : dist) if (!Double.isInfinite(d) && !Double.isNaN(d)) { sum += d; cnt++; }
        return cnt == 0 ? null : (sum / cnt);
    }
    public static Double maxFinite(double[] dist) {
        double best = Double.NEGATIVE_INFINITY; boolean any = false;
        for (double d : dist) if (!Double.isInfinite(d) && !Double.isNaN(d)) { any = true; if (d > best) best = d; }
        return any ? best : null;
    }
    public static double millisFromNanos(long t0, long t1) {
        // Преобразуем наносекунды в миллисекунды с плавающей точкой
        double ms = (t1 - t0) / 1_000_000.0;
        // Если очень маленькое — показываем хотя бы 0.000001
        return Math.max(ms, 0.000001);
    }

}
