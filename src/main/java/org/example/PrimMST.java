
package org.example;

import java.util.*;

public class PrimMST {

    public static class Result {
        public final List<Graph.Edge> mstEdges;
        public final Double totalCost;
        public final String error;

        public Result(List<Graph.Edge> mstEdges, Double totalCost, String error) {
            this.mstEdges = mstEdges;
            this.totalCost = totalCost;
            this.error = error;
        }
        public boolean ok() { return error == null; }
    }

    private final MetricsIO.OperationCounter counter;

    public PrimMST(MetricsIO.OperationCounter counter) {
        this.counter = counter;
    }

    public Result compute(Graph g) {
        Set<String> V = g.getVertices();
        if (V.isEmpty()) return new Result(new ArrayList<>(), 0.0, null);

        Map<String, List<Graph.Edge>> adj = g.adj();
        String start = V.iterator().next();

        Set<String> vis = new HashSet<>();
        vis.add(start);
        List<Graph.Edge> mst = new ArrayList<>();
        double cost = 0.0;

        PriorityQueue<Graph.Edge> pq = new PriorityQueue<>((a,b) -> {
            counter.cmp();
            return Double.compare(a.w, b.w);
        });
        for (var e : adj.getOrDefault(start, List.of())) { pq.offer(e); counter.heap(); }

        while (!pq.isEmpty() && mst.size() < V.size()-1) {
            Graph.Edge e = pq.poll(); counter.heap();
            String u = e.u, v = e.v;
            boolean iu = vis.contains(u), iv = vis.contains(v);
            if (iu && iv) continue;
            String nv = iu ? v : u;
            mst.add(e);
            cost += e.w;
            vis.add(nv);
            for (var ne : adj.getOrDefault(nv, List.of())) {
                String other = ne.other(nv);
                if (other != null && !vis.contains(other)) { pq.offer(ne); counter.heap(); }
            }
        }

        if (mst.size() != V.size()-1) return new Result(List.of(), null, "Graph is disconnected (no MST).");
        return new Result(mst, cost, null);
    }
}
