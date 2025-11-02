package smart.scheduling.common.util;

import smart.scheduling.common.metrics.Metrics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class MetricsExporter {
    public static void saveToCsv(Metrics metrics, String fileName) {
        Map<String, Object> snapshot = metrics.snapshot();
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write("metric,value\n");
            for (var e : snapshot.entrySet()) {
                writer.write(e.getKey() + "," + e.getValue() + "\n");
            }
            System.out.println("✅ Metrics exported to " + fileName);
        } catch (IOException e) {
            System.err.println("❌ Failed to export metrics: " + e.getMessage());
        }
    }

}
