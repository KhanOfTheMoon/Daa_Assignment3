package org.example;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MetricsIO {

    public static class OutputEntry {
        public int graph_id;
        public InputStats input_stats = new InputStats();
        public AlgoResult prim = new AlgoResult();
        public AlgoResult kruskal = new AlgoResult();
    }

    public static class InputStats {
        public int vertices;
        public int edges;
    }

    public static class AlgoResult {
        public List<Map<String,Object>> mst_edges = new ArrayList<>();
        public Double total_cost;
        public long operations_count;
        public double execution_time_ms;
        public String error;
    }

    public static void writeJson(String path, List<OutputEntry> results) throws Exception {
        ObjectMapper m = new ObjectMapper();
        ObjectWriter w = m.writer(new DefaultPrettyPrinter()
                .withObjectIndenter(new DefaultPrettyPrinter.FixedSpaceIndenter()));
        w = w.withFeatures(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        var wrap = new Object(){ public List<OutputEntry> results = results; };
        w.writeValue(new File(path), wrap);
    }

    public static void writeCsv(String path, List<OutputEntry> results) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("graph_id,vertices,edges,prim_total_cost,kruskal_total_cost,prim_ms,kruskal_ms,prim_ops,kruskal_ops,equal_cost,error\n");
        for (var e : results) {
            String primCost = e.prim.total_cost == null ? "" : String.valueOf(e.prim.total_cost);
            String krCost   = e.kruskal.total_cost == null ? "" : String.valueOf(e.kruskal.total_cost);
            boolean equal = (e.prim.total_cost != null && e.prim.total_cost.equals(e.kruskal.total_cost));
            String error = "";
            if (e.prim.error != null) error = "prim:" + e.prim.error;
            if (e.kruskal.error != null) error = (error.isEmpty() ? "" : error + " | ") + "kruskal:" + e.kruskal.error;

            sb.append(e.graph_id).append(",")
                    .append(e.input_stats.vertices).append(",")
                    .append(e.input_stats.edges).append(",")
                    .append(primCost).append(",")
                    .append(krCost).append(",")
                    .append(String.format(Locale.US, "%.3f", e.prim.execution_time_ms)).append(",")
                    .append(String.format(Locale.US, "%.3f", e.kruskal.execution_time_ms)).append(",")
                    .append(e.prim.operations_count).append(",")
                    .append(e.kruskal.operations_count).append(",")
                    .append(equal).append(",")
                    .append("\"").append(error.replace("\"","''")).append("\"")
                    .append("\n");
        }
        Files.writeString(Path.of(path), sb.toString());
    }

    // Счётчик операций
    public static class OperationCounter {
        private long comparisons = 0;
        private long heapOps = 0;
        private long ufFind = 0;
        private long ufUnion = 0;

        public void cmp()   { comparisons++; }
        public void heap()  { heapOps++; }
        public void ufFind(){ ufFind++; }
        public void ufUnion(){ ufUnion++; }

        public long total() { return comparisons + heapOps + ufFind + ufUnion; }

        // геттеры для тестов
        public long getComparisons(){ return comparisons; }
        public long getHeapOps(){ return heapOps; }
        public long getUfFind(){ return ufFind; }
        public long getUfUnion(){ return ufUnion; }
    }
}
