package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Starter {

    public static void main(String[] args) throws Exception {
        List<String[]> jobs = collectJobs(args);
        if (jobs.isEmpty()) {
            System.out.println("No inputs. Put *.json with \"graphs\" in the working dir or pass file names as args.");
            return;
        }
        for (String[] j : jobs) runOne(j[0], j[1], j[2]);
    }

    // ---- job collection ----
    private static List<String[]> collectJobs(String[] args) throws Exception {
        List<String[]> jobs = new ArrayList<>();
        if (args.length == 0) {
            // взять все *.json (кроме *_out.json) из текущей папки
            try (var stream = Files.list(Path.of("."))) {
                var files = stream
                        .filter(p -> {
                            String name = p.getFileName().toString().toLowerCase();
                            return name.endsWith(".json") && !name.endsWith("_out.json");
                        })
                        .sorted()
                        .collect(Collectors.toList());
                for (Path p : files) jobs.add(deriveOut(p.toString()));
            }
        } else if (args.length % 3 == 0) {
            // явные тройки: in out csv
            for (int i = 0; i < args.length; i += 3)
                jobs.add(new String[]{ args[i], args[i+1], args[i+2] });
        } else {
            // список входов: out/csv подставим автоматически
            for (String in : args) jobs.add(deriveOut(in));
        }
        return jobs;
    }

    private static String[] deriveOut(String in) {
        String base = Path.of(in).getFileName().toString();
        int dot = base.lastIndexOf('.');
        String stem = dot >= 0 ? base.substring(0, dot) : base;
        return new String[]{ in, stem + "_out.json", stem + "_results.csv" };
    }

    // ---- one run ----
    private static void runOne(String inputPath, String outputPath, String csvPath) throws Exception {
        System.out.println("Processing: " + inputPath);
        JsonInput.InputData input;
        try {
            input = JsonInput.read(inputPath);
        } catch (Exception e) {
            System.out.println("  Skip (not a dataset or bad JSON): " + inputPath);
            return;
        }
        if (input.graphs == null || input.graphs.isEmpty()) {
            System.out.println("  Skip (no graphs): " + inputPath);
            return;
        }

        List<MetricsIO.OutputEntry> results = new ArrayList<>();
        for (var g : input.graphs) {
            Graph graph = Graph.fromInput(g);

            var entry = new MetricsIO.OutputEntry();
            entry.graph_id = g.graph_id;
            entry.input_stats.vertices = graph.getVertices().size();
            entry.input_stats.edges    = graph.getEdges().size();

            // Prim
            var pc = new MetricsIO.OperationCounter();
            long t0 = System.nanoTime();
            var pr  = new PrimMST(pc).compute(graph);
            long t1 = System.nanoTime();
            entry.prim.execution_time_ms = (t1 - t0) / 1_000_000.0;
            entry.prim.operations_count  = pc.total();
            if (pr.ok()) { entry.prim.mst_edges = toEdgeMaps(pr.mstEdges); entry.prim.total_cost = pr.totalCost; }
            else         { entry.prim.error = pr.error; }

            // Kruskal
            var kc = new MetricsIO.OperationCounter();
            long k0 = System.nanoTime();
            var kr  = new KruskalMST(kc).compute(graph);
            long k1 = System.nanoTime();
            entry.kruskal.execution_time_ms = (k1 - k0) / 1_000_000.0;
            entry.kruskal.operations_count  = kc.total();
            if (kr.ok()) { entry.kruskal.mst_edges = toEdgeMaps(kr.mstEdges); entry.kruskal.total_cost = kr.totalCost; }
            else         { entry.kruskal.error = kr.error; }

            results.add(entry);
        }

        MetricsIO.writeJson(outputPath, results);
        MetricsIO.writeCsv(csvPath,   results);
        System.out.println("Done → JSON: " + outputPath + " | CSV: " + csvPath + "\n");
    }

    private static List<Map<String,Object>> toEdgeMaps(List<Graph.Edge> edges) {
        List<Map<String,Object>> list = new ArrayList<>();
        for (var e : edges) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("from", e.u);
            m.put("to", e.v);
            m.put("weight", e.w);
            list.add(m);
        }
        return list;
    }
}
