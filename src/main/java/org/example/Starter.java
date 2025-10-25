package org.example;

import java.util.*;
import java.util.LinkedHashMap;

public class Starter {

    public static void main(String[] args) throws Exception {
        String inputPath  = args.length > 0 ? args[0] : "assign_3_input.json";
        String outputPath = args.length > 1 ? args[1] : "output.json";
        String csvPath    = args.length > 2 ? args[2] : "results.csv";

        JsonInput.InputData input = JsonInput.read(inputPath);
        List<MetricsIO.OutputEntry> results = new ArrayList<>();

        for (JsonInput.InputGraph g : input.graphs) {
            Graph graph = Graph.fromInput(g);
            MetricsIO.OutputEntry entry = new MetricsIO.OutputEntry();
            entry.graph_id = g.graph_id;
            entry.input_stats.vertices = graph.getVertices().size();
            entry.input_stats.edges = graph.getEdges().size();

            // Prim
            MetricsIO.OperationCounter primCounter = new MetricsIO.OperationCounter();
            PrimMST prim = new PrimMST(primCounter);
            long t0 = System.nanoTime();
            PrimMST.Result pr = prim.compute(graph);
            long t1 = System.nanoTime();
            entry.prim.execution_time_ms = (t1 - t0) / 1_000_000.0;
            entry.prim.operations_count  = primCounter.total();
            if (pr.ok()) {
                entry.prim.mst_edges = toEdgeMaps(pr.mstEdges);
                entry.prim.total_cost = pr.totalCost;
            } else {
                entry.prim.error = pr.error;
            }

            // Kruskal
            MetricsIO.OperationCounter krCounter = new MetricsIO.OperationCounter();
            KruskalMST kr = new KruskalMST(krCounter);
            long k0 = System.nanoTime();
            KruskalMST.Result rr = kr.compute(graph);
            long k1 = System.nanoTime();
            entry.kruskal.execution_time_ms = (k1 - k0) / 1_000_000.0;
            entry.kruskal.operations_count  = krCounter.total();
            if (rr.ok()) {
                entry.kruskal.mst_edges = toEdgeMaps(rr.mstEdges);
                entry.kruskal.total_cost = rr.totalCost;
            } else {
                entry.kruskal.error = rr.error;
            }

            results.add(entry);
        }

        MetricsIO.writeJson(outputPath, results);
        MetricsIO.writeCsv(csvPath, results);
        System.out.println("Done.\nJSON: " + outputPath + "\nCSV: " + csvPath);
    }

    private static List<Map<String,Object>> toEdgeMaps(List<Graph.Edge> edges) {
        List<Map<String,Object>> list = new ArrayList<>();
        for (Graph.Edge e : edges) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("from", e.u);
            m.put("to", e.v);
            m.put("weight", e.w);
            list.add(m);
        }
        return list;
    }
}
