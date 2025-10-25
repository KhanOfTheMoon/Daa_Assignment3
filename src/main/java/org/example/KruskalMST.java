package org.example;

import java.util.*;

public class KruskalMST {

    public static class Result {
        public final List<Graph.Edge> mstEdges;
        public final Double totalCost;
        public final String error;
        public Result(List<Graph.Edge> e, Double c, String err) { mstEdges = e; totalCost = c; error = err; }
        public boolean ok() { return error == null; }
    }

    private final MetricsIO.OperationCounter counter;
    public KruskalMST(MetricsIO.OperationCounter counter) { this.counter = counter; }

    public Result compute(Graph g) {
        List<Graph.Edge> edges = new ArrayList<>(g.getEdges());
        edges.sort((a,b)->{ counter.cmp(); return Double.compare(a.w, b.w); });

        Map<String,Integer> idx = new LinkedHashMap<>();
        int id = 0; for (String v : g.getVertices()) idx.put(v, id++);
        UnionFind uf = new UnionFind(g.getVertices().size(), counter);

        List<Graph.Edge> mst = new ArrayList<>();
        double cost = 0.0;

        for (var e : edges) {
            int u = idx.get(e.u), v = idx.get(e.v);
            if (uf.find(u) != uf.find(v)) {
                uf.union(u, v);
                mst.add(e);
                cost += e.w;
                if (mst.size() == g.getVertices().size()-1) break;
            }
        }
        if (mst.size() != g.getVertices().size()-1) return new Result(List.of(), null, "Graph is disconnected (no MST).");
        return new Result(mst, cost, null);
    }

    // Встроенный Union-Find
    static class UnionFind {
        final int[] p, r;
        final MetricsIO.OperationCounter c;
        UnionFind(int n, MetricsIO.OperationCounter c){ this.c = c; p = new int[n]; r = new int[n]; for(int i=0;i<n;i++){p[i]=i;r[i]=0;}}
        int find(int x){ c.ufFind(); if(p[x]!=x) p[x]=find(p[x]); return p[x]; }
        void union(int a,int b){ c.ufUnion(); int ra=find(a), rb=find(b);
            if(ra==rb) return;
            if(r[ra]<r[rb]) p[ra]=rb;
            else if(r[ra]>r[rb]) p[rb]=ra;
            else { p[rb]=ra; r[ra]++; }
        }
    }
}
