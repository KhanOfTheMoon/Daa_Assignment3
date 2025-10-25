package org.example;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class CorrectnessTest {

    // --- helpers ---
    private static boolean isConnected(Set<String> verts, List<Graph.Edge> mstEdges) {
        if (verts.isEmpty()) return true;
        Map<String, List<String>> adj = new HashMap<>();
        for (String v : verts) adj.put(v, new ArrayList<>());
        for (var e : mstEdges) { adj.get(e.u).add(e.v); adj.get(e.v).add(e.u); }
        Set<String> seen = new HashSet<>();
        Deque<String> dq = new ArrayDeque<>();
        String start = verts.iterator().next();
        seen.add(start); dq.add(start);
        while (!dq.isEmpty()) {
            String x = dq.poll();
            for (String y : adj.get(x)) if (seen.add(y)) dq.add(y);
        }
        return seen.size() == verts.size();
    }

    private static boolean hasCycle(Set<String> verts, List<Graph.Edge> mstEdges) {
        Map<String,Integer> idx = new LinkedHashMap<>();
        int id = 0; for (String v : verts) idx.put(v, id++);
        int[] p = new int[idx.size()], r = new int[idx.size()];
        for (int i=0;i<p.length;i++){ p[i]=i; r[i]=0; }
        java.util.function.IntUnaryOperator find = new java.util.function.IntUnaryOperator() {
            @Override public int applyAsInt(int x){ return p[x]==x?x:(p[x]=applyAsInt(p[x])); }
        };
        java.util.function.BiConsumer<Integer,Integer> uni = (a,b)->{
            int ra = find.applyAsInt(a), rb = find.applyAsInt(b);
            if (ra==rb) return;
            if (r[ra]<r[rb]) p[ra]=rb;
            else if (r[ra]>r[rb]) p[rb]=ra;
            else { p[rb]=ra; r[ra]++; }
        };
        for (var e : mstEdges) {
            int u = idx.get(e.u), v = idx.get(e.v);
            int ru = find.applyAsInt(u), rv = find.applyAsInt(v);
            if (ru == rv) return true;
            uni.accept(ru, rv);
        }
        return false;
    }

    private Graph sample() {
        Graph g = new Graph();
        g.addEdge("A","B",4);
        g.addEdge("A","C",3);
        g.addEdge("B","C",2);
        g.addEdge("B","D",5);
        g.addEdge("C","D",7);
        g.addEdge("C","E",8);
        g.addEdge("D","E",6);
        return g;
    }

    @Test
    void mstCorrectness() {
        Graph g = sample();
        var pr = new PrimMST(new MetricsIO.OperationCounter()).compute(g);
        var kr = new KruskalMST(new MetricsIO.OperationCounter()).compute(g);

        assertTrue(pr.ok() && kr.ok());
        assertEquals(pr.totalCost, kr.totalCost, 1e-9);

        int expected = g.getVertices().size() - 1;
        assertEquals(expected, pr.mstEdges.size());
        assertEquals(expected, kr.mstEdges.size());

        assertTrue(isConnected(g.getVertices(), pr.mstEdges));
        assertTrue(isConnected(g.getVertices(), kr.mstEdges));
        assertFalse(hasCycle(g.getVertices(), pr.mstEdges));
        assertFalse(hasCycle(g.getVertices(), kr.mstEdges));
    }

    @Test
    void handlesDisconnected() {
        Graph g = new Graph();
        g.addEdge("A","B",1);
        g.addEdge("C","D",2); // disconnected

        var pr = new PrimMST(new MetricsIO.OperationCounter()).compute(g);
        var kr = new KruskalMST(new MetricsIO.OperationCounter()).compute(g);

        assertFalse(pr.ok());
        assertFalse(kr.ok());
        assertNotNull(pr.error);
        assertNotNull(kr.error);
    }
}
