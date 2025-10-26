package org.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MetricsIO {

    // единица результата по одному графу
    public static class OutputEntry {
        public int graph_id;
        public InputStats input_stats = new InputStats();
        public AlgoResult prim = new AlgoResult();
        public AlgoResult kruskal = new AlgoResult();
    }

    // статистика входного графа
    public static class InputStats {
        public int vertices;
        public int edges;
    }

    // результат алгоритма по графу
    public static class AlgoResult {
        public List<Map<String,Object>> mst_edges = new ArrayList<>();
        public Double total_cost;
        public long operations_count;
        public Double execution_time_ms; // Double, чтобы при необходимости можно было опустить поле
        public String error;             // Jackson не выведет это поле, если оно null
    }

    // сводка по датасету
    public static class DatasetSummary {
        public String dataset;
        public int graphs_connected;
        public double avg_prim_ms;
        public double avg_kruskal_ms;
        public double avg_prim_ops;
        public double avg_kruskal_ops;
        public boolean kruskal_faster;
        public boolean all_equal_cost;
    }

    // JSON как в примере: pretty-print и пропускать null-поля
    public static void writeJson(String path, List<OutputEntry> results) throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ObjectWriter w = m.writer(new DefaultPrettyPrinter());
        Map<String, Object> wrap = new LinkedHashMap<>();
        wrap.put("results", results);
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
                    .append(e.prim.execution_time_ms == null ? "" : String.format(Locale.US, "%.2f", e.prim.execution_time_ms)).append(",")
                    .append(e.kruskal.execution_time_ms == null ? "" : String.format(Locale.US, "%.2f", e.kruskal.execution_time_ms)).append(",")
                    .append(e.prim.operations_count).append(",")
                    .append(e.kruskal.operations_count).append(",")
                    .append(equal).append(",")
                    .append("\"").append(error.replace("\"","''")).append("\"")
                    .append("\n");
        }
        Files.writeString(Path.of(path), sb.toString());
    }

    // агрегированная сводка по одному датасету
    public static DatasetSummary computeSummary(List<OutputEntry> results, String datasetName) {
        DatasetSummary s = new DatasetSummary();
        s.dataset = datasetName;

        double sumPrimMs = 0, sumKrMs = 0, sumPrimOps = 0, sumKrOps = 0;
        int connected = 0;
        boolean allEqual = true;

        for (OutputEntry e : results) {
            boolean ok = e.prim.total_cost != null && e.kruskal.total_cost != null;
            if (!ok) { allEqual = false; continue; }

            connected++;
            sumPrimMs  += e.prim.execution_time_ms == null ? 0.0 : e.prim.execution_time_ms;
            sumKrMs    += e.kruskal.execution_time_ms == null ? 0.0 : e.kruskal.execution_time_ms;
            sumPrimOps += e.prim.operations_count;
            sumKrOps   += e.kruskal.operations_count;

            if (Math.abs(e.prim.total_cost - e.kruskal.total_cost) >= 1e-9) {
                allEqual = false;
            }
        }

        s.graphs_connected = connected;
        if (connected > 0) {
            s.avg_prim_ms     = sumPrimMs  / connected;
            s.avg_kruskal_ms  = sumKrMs    / connected;
            s.avg_prim_ops    = sumPrimOps / connected;
            s.avg_kruskal_ops = sumKrOps   / connected;
        } else {
            s.avg_prim_ms = s.avg_kruskal_ms = s.avg_prim_ops = s.avg_kruskal_ops = 0.0;
        }
        s.kruskal_faster = s.avg_kruskal_ms <= s.avg_prim_ms;
        s.all_equal_cost = allEqual;
        return s;
    }

    // единый CSV по всем датасетам
    public static void writeMasterSummaryCsv(String path, List<DatasetSummary> rows) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("dataset,graphs_connected,avg_prim_ms,avg_kruskal_ms,avg_prim_ops,avg_kruskal_ops,kruskal_faster,all_equal_cost\n");

        int totalConn = 0;
        double sumPrimMs = 0, sumKrMs = 0, sumPrimOps = 0, sumKrOps = 0;
        boolean allEqual = true;

        for (DatasetSummary s : rows) {
            String name = (s.dataset == null) ? "" : s.dataset;
            sb.append(name).append(",")
                    .append(s.graphs_connected).append(",")
                    .append(fmt4(s.avg_prim_ms)).append(",")
                    .append(fmt4(s.avg_kruskal_ms)).append(",")
                    .append(fmt1(s.avg_prim_ops)).append(",")
                    .append(fmt1(s.avg_kruskal_ops)).append(",")
                    .append(s.kruskal_faster).append(",")
                    .append(s.all_equal_cost).append("\n");

            totalConn += s.graphs_connected;
            sumPrimMs  += s.avg_prim_ms     * s.graphs_connected;
            sumKrMs    += s.avg_kruskal_ms  * s.graphs_connected;
            sumPrimOps += s.avg_prim_ops    * s.graphs_connected;
            sumKrOps   += s.avg_kruskal_ops * s.graphs_connected;
            if (!s.all_equal_cost) allEqual = false;
        }

        double ovPrimMs  = totalConn == 0 ? 0 : sumPrimMs  / totalConn;
        double ovKrMs    = totalConn == 0 ? 0 : sumKrMs    / totalConn;
        double ovPrimOps = totalConn == 0 ? 0 : sumPrimOps / totalConn;
        double ovKrOps   = totalConn == 0 ? 0 : sumKrOps   / totalConn;
        boolean ovKruskalFaster = ovKrMs <= ovPrimMs;

        sb.append("OVERALL,")
                .append(totalConn).append(",")
                .append(fmt4(ovPrimMs)).append(",")
                .append(fmt4(ovKrMs)).append(",")
                .append(fmt1(ovPrimOps)).append(",")
                .append(fmt1(ovKrOps)).append(",")
                .append(ovKruskalFaster).append(",")
                .append(allEqual).append("\n");

        Files.writeString(Path.of(path), sb.toString());
    }

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

        public long getComparisons(){ return comparisons; }
        public long getHeapOps(){ return heapOps; }
        public long getUfFind(){ return ufFind; }
        public long getUfUnion(){ return ufUnion; }
    }

    private static String fmt4(double v){ return String.format(Locale.US, "%.4f", v); }
    private static String fmt1(double v){ return String.format(Locale.US, "%.1f", v); }
}
