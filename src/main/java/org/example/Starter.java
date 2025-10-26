package org.example;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Starter {

    public static void main(String[] args) throws Exception {
        List<String> inputs = collectInputs(args);
        if (inputs.isEmpty()) {
            System.out.println("No inputs. Put assign_3_small/medium/large.json in working dir or pass them as args.");
            return;
        }

        List<MetricsIO.DatasetSummary> master = new ArrayList<>();
        for (String in : inputs) {
            String label = detectLabel(in); // small/medium/large
            if (label == null) {
                System.out.println("Skip (name must contain small/medium/large): " + in);
                continue;
            }
            String outJson = "assign_3_" + label + "_out.json";
            String outCsv  = "assign_3_" + label + "_results.csv";

            MetricsIO.DatasetSummary s = runOne(in, outJson, outCsv, label);
            if (s != null) master.add(s);
        }

        MetricsIO.writeMasterSummaryCsv("datasets_summary.csv", master);
        System.out.println("MASTER: datasets_summary.csv");
    }

    private static List<String> collectInputs(String[] args) throws Exception {
        List<String> ins = new ArrayList<>();
        if (args != null && args.length > 0) {
            for (String a : args) ins.add(a);
            return ins;
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Path.of("."), "*.json")) {
            for (Path p : ds) {
                String name = p.getFileName().toString().toLowerCase();
                if (name.contains("small") || name.contains("medium") || name.contains("large")) {
                    ins.add(p.getFileName().toString());
                }
            }
        }
        Collections.sort(ins);
        return ins;
    }

    private static String detectLabel(String path) {
        String name = Path.of(path).getFileName().toString().toLowerCase();
        if (name.contains("small"))  return "small";
        if (name.contains("medium")) return "medium";
        if (name.contains("large"))  return "large";
        return null;
    }

    private static MetricsIO.DatasetSummary runOne(String inputPath, String outputJson, String outputCsv, String datasetLabel) throws Exception {
        System.out.println("Processing: " + inputPath);

        JsonInput.InputData input;
        try {
            input = JsonInput.read(inputPath);
        } catch (Exception e) {
            System.out.println("  Skip (bad JSON): " + inputPath);
            return null;
        }
        if (input.graphs == null || input.graphs.isEmpty()) {
            System.out.println("  Skip (no graphs): " + inputPath);
            return null;
        }

        List<MetricsIO.OutputEntry> results = new ArrayList<>();

        for (JsonInput.InputGraph g : input.graphs) {
            Graph graph = Graph.fromInput(g);

            MetricsIO.OutputEntry entry = new MetricsIO.OutputEntry();
            entry.graph_id = g.graph_id;
            entry.input_stats.vertices = graph.getVertices().size();
            entry.input_stats.edges    = graph.getEdges().size();

            MetricsIO.OperationCounter pc = new MetricsIO.OperationCounter();
            long t0 = System.nanoTime();
            PrimMST.Result pr = new PrimMST(pc).compute(graph);
            long t1 = System.nanoTime();
            entry.prim.execution_time_ms = (t1 - t0) / 1_000_000.0;
            entry.prim.operations_count  = pc.total();
            if (pr.ok()) {
                entry.prim.mst_edges  = toEdgeMaps(pr.mstEdges);
                entry.prim.total_cost = pr.totalCost;
            } else {
                entry.prim.error = pr.error;
            }

            MetricsIO.OperationCounter kc = new MetricsIO.OperationCounter();
            long k0 = System.nanoTime();
            KruskalMST.Result kr = new KruskalMST(kc).compute(graph);
            long k1 = System.nanoTime();
            entry.kruskal.execution_time_ms = (k1 - k0) / 1_000_000.0;
            entry.kruskal.operations_count  = kc.total();
            if (kr.ok()) {
                entry.kruskal.mst_edges  = toEdgeMaps(kr.mstEdges);
                entry.kruskal.total_cost = kr.totalCost;
            } else {
                entry.kruskal.error = kr.error;
            }

            results.add(entry);
        }

        MetricsIO.writeJson(outputJson, results);
        MetricsIO.writeCsvSimple(outputCsv, results);

        System.out.println("Done → JSON: " + outputJson + " | CSV: " + outputCsv);

        return MetricsIO.computeSummary(results, datasetLabel);
    }

    private static List<Map<String,Object>> toEdgeMaps(List<Graph.Edge> edges) {
        List<Map<String,Object>> list = new ArrayList<>();
        for (Graph.Edge e : edges) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("from", e.u);
            m.put("to",   e.v);
            m.put("weight", e.w);
            list.add(m);
        }
        return list;
    }
}
