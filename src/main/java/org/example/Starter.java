package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Starter {

    public static void main(String[] args) throws Exception {
        List<String[]> jobs = collectJobs(args);
        if (jobs.isEmpty()) {
            System.out.println("No inputs. Pass file names as args or put *.json with 'graphs' in the working dir.");
            return;
        }

        List<MetricsIO.DatasetSummary> master = new ArrayList<>();

        for (String[] j : jobs) {
            MetricsIO.DatasetSummary s = runOneDataset(j[0], j[1], j[2]);
            if (s != null) master.add(s);
        }

        MetricsIO.writeMasterSummaryCsv("datasets_summary.csv", master);
        System.out.println("MASTER summary: datasets_summary.csv");
    }

    // собираем задания: либо тройки (in,out,csv), либо просто список входов, либо авто-скан текущей папки
    private static List<String[]> collectJobs(String[] args) throws Exception {
        List<String[]> jobs = new ArrayList<>();

        if (args.length == 0) {
            try (var stream = Files.list(Path.of("."))) {
                var files = stream
                        .filter(p -> {
                            String name = p.getFileName().toString().toLowerCase();
                            return name.endsWith(".json") && !name.endsWith("_out.json");
                        })
                        .sorted()
                        .collect(Collectors.toList());
                for (Path p : files) {
                    jobs.add(deriveOutNames(p.toString()));
                }
            }
        } else if (args.length % 3 == 0) {
            for (int i = 0; i < args.length; i += 3) {
                jobs.add(new String[]{ args[i], args[i+1], args[i+2] });
            }
        } else {
            for (String in : args) {
                jobs.add(deriveOutNames(in));
            }
        }
        return jobs;
    }

    // in.json -> in_out.json, in_results.csv
    private static String[] deriveOutNames(String in) {
        String base = Path.of(in).getFileName().toString();
        int dot = base.lastIndexOf('.');
        String stem = dot >= 0 ? base.substring(0, dot) : base;
        return new String[]{ in, stem + "_out.json", stem + "_results.csv" };
    }

    private static MetricsIO.DatasetSummary runOneDataset(String inputPath, String outputPath, String csvPath) throws Exception {
        System.out.println("Processing: " + inputPath);
        JsonInput.InputData input;
        try {
            input = JsonInput.read(inputPath);
        } catch (Exception e) {
            System.out.println("  Skip (bad JSON?): " + inputPath);
            return null;
        }
        if (input.graphs == null || input.graphs.isEmpty()) {
            System.out.println("  Skip (no graphs): " + inputPath);
            return null;
        }

        List<MetricsIO.OutputEntry> results = new ArrayList<>();

        for (var g : input.graphs) {
            Graph graph = Graph.fromInput(g);

            MetricsIO.OutputEntry entry = new MetricsIO.OutputEntry();
            entry.graph_id = g.graph_id;
            entry.input_stats.vertices = graph.getVertices().size();
            entry.input_stats.edges    = graph.getEdges().size();

            MetricsIO.OperationCounter pc = new MetricsIO.OperationCounter();
            long t0 = System.nanoTime();
            var pr  = new PrimMST(pc).compute(graph);
            long t1 = System.nanoTime();
            entry.prim.execution_time_ms = round2((t1 - t0) / 1_000_000.0);
            entry.prim.operations_count  = pc.total();
            if (pr.ok()) {
                entry.prim.mst_edges  = toEdgeMapsForJson(pr.mstEdges);
                entry.prim.total_cost = pr.totalCost;
            } else {
                entry.prim.error = pr.error;
            }

            MetricsIO.OperationCounter kc = new MetricsIO.OperationCounter();
            long k0 = System.nanoTime();
            var kr  = new KruskalMST(kc).compute(graph);
            long k1 = System.nanoTime();
            entry.kruskal.execution_time_ms = round2((k1 - k0) / 1_000_000.0);
            entry.kruskal.operations_count  = kc.total();
            if (kr.ok()) {
                entry.kruskal.mst_edges  = toEdgeMapsForJson(kr.mstEdges);
                entry.kruskal.total_cost = kr.totalCost;
            } else {
                entry.kruskal.error = kr.error;
            }

            results.add(entry);
        }

        MetricsIO.writeJson(outputPath, results);
        MetricsIO.writeCsv(csvPath,   results);

        String datasetName = stemOf(csvPath.replace("_results.csv","").replace("_out",""));
        MetricsIO.DatasetSummary summary = MetricsIO.computeSummary(results, datasetName);

        System.out.println("Done → JSON: " + outputPath + " | CSV: " + csvPath);
        return summary;
    }

    private static String stemOf(String path) {
        String base = Path.of(path).getFileName().toString();
        int dot = base.lastIndexOf('.');
        return dot >= 0 ? base.substring(0, dot) : base;
    }

    private static double round2(double x) {
        return Math.round(x * 100.0) / 100.0;
    }

    // приводим веса в JSON к "2" вместо "2.0" и округляем дробные веса до 2 знаков
    private static List<Map<String,Object>> toEdgeMapsForJson(List<Graph.Edge> edges) {
        List<Map<String,Object>> list = new ArrayList<>();
        for (Graph.Edge e : edges) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("from", e.u);
            m.put("to",   e.v);

            double w = e.w;
            if (Math.abs(w - Math.rint(w)) < 1e-9) {
                m.put("weight", (long) Math.rint(w));
            } else {
                m.put("weight", round2(w));
            }
            list.add(m);
        }
        return list;
    }
}
