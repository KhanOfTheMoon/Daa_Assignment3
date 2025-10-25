package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PerfTest {

    private Graph chain(int n) {
        Graph g = new Graph();
        for (int i=1;i<=n;i++) g.addEdge("V"+i, "V"+(i+1), i);
        return g;
    }

    @Test
    void perfAndConsistency() {
        Graph g = chain(30);

        MetricsIO.OperationCounter c1 = new MetricsIO.OperationCounter();
        long t0 = System.nanoTime();
        var pr1 = new PrimMST(c1).compute(g);
        long t1 = System.nanoTime();
        double ms1 = (t1 - t0) / 1_000_000.0;

        MetricsIO.OperationCounter c2 = new MetricsIO.OperationCounter();
        long t2 = System.nanoTime();
        var pr2 = new PrimMST(c2).compute(g);
        long t3 = System.nanoTime();
        double ms2 = (t3 - t2) / 1_000_000.0;

        MetricsIO.OperationCounter c3 = new MetricsIO.OperationCounter();
        long t4 = System.nanoTime();
        var kr1 = new KruskalMST(c3).compute(g);
        long t5 = System.nanoTime();
        double ms3 = (t5 - t4) / 1_000_000.0;

        MetricsIO.OperationCounter c4 = new MetricsIO.OperationCounter();
        long t6 = System.nanoTime();
        var kr2 = new KruskalMST(c4).compute(g);
        long t7 = System.nanoTime();
        double ms4 = (t7 - t6) / 1_000_000.0;

        assertTrue(ms1 >= 0 && ms2 >= 0 && ms3 >= 0 && ms4 >= 0);
        assertTrue(c1.total() >= 0 && c2.total() >= 0 && c3.total() >= 0 && c4.total() >= 0);
        assertTrue(c1.getComparisons() >= 0 && c1.getHeapOps() >= 0);
        assertTrue(c3.getUfFind() >= 0 && c3.getUfUnion() >= 0);

        assertTrue(pr1.ok() && pr2.ok() && kr1.ok() && kr2.ok());
        assertEquals(pr1.totalCost, pr2.totalCost, 1e-9);
        assertEquals(kr1.totalCost, kr2.totalCost, 1e-9);
        assertEquals(pr1.mstEdges.size(), pr2.mstEdges.size());
        assertEquals(kr1.mstEdges.size(), kr2.mstEdges.size());

        assertEquals(c1.total(), c2.total());
        assertEquals(c3.total(), c4.total());
    }
}
