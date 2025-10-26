Report — Optimization of a City Transportation Network (MST)

1. Summary of input data and per-case results

Dataset summary (connected graphs only averaged):

| dataset | graphs_connected | avg_prim_ms | avg_kruskal_ms | avg_prim_ops | avg_kruskal_ops | kruskal_faster | all_equal_cost |
| ------- | ---------------: | ----------: | -------------: | -----------: | --------------: | -------------: | -------------: |
| large   |                4 |      0.5268 |         0.3423 |        329.0 |           348.0 |           true |           true |
| medium  |                4 |      0.0603 |         0.0338 |        158.0 |           173.0 |           true |          false |
| small   |                5 |      0.0192 |         0.0146 |         25.2 |            38.6 |           true |          false |
| OVERALL |               13 |      0.1880 |         0.1213 |        159.5 |           175.2 |           true |          false |

Small (4–6 vertices)

| id |  V |  E | Prim cost | Prim ms | Prim ops | Kruskal cost | Kruskal ms | Kruskal ops | note                  |
| -: | -: | -: | --------: | ------: | -------: | -----------: | ---------: | ----------: | --------------------- |
|  1 |  5 |  4 |      14.0 |   0.031 |        8 |         14.0 |      0.023 |          29 |                       |
|  2 |  5 |  7 |      15.0 |   0.017 |       24 |         15.0 |      0.012 |          45 |                       |
|  3 |  4 |  6 |       9.0 |   0.011 |       19 |          9.0 |      0.007 |          20 |                       |
|  4 |  6 | 13 |      10.0 |   0.019 |       50 |         10.0 |      0.016 |          60 |                       |
|  5 |  6 |  3 |         — |   0.010 |        4 |            — |      0.010 |          21 | disconnected (no MST) |
|  6 |  5 |  7 |      16.0 |   0.018 |       25 |         16.0 |      0.015 |          39 |                       |

Medium (10–15 vertices)

|  id |  V |  E | Prim cost | Prim ms | Prim ops | Kruskal cost | Kruskal ms | Kruskal ops | note                  |
| --: | -: | -: | --------: | ------: | -------: | -----------: | ---------: | ----------: | --------------------- |
| 101 | 10 | 17 |      33.0 |   0.076 |       71 |         33.0 |      0.047 |         106 |                       |
| 102 | 12 | 22 |      40.0 |   0.041 |      106 |         40.0 |      0.023 |         159 |                       |
| 103 | 15 | 40 |      39.0 |   0.061 |      207 |         39.0 |      0.034 |         202 |                       |
| 104 | 11 |  4 |         — |   0.013 |        4 |            — |      0.014 |          28 | disconnected (no MST) |
| 105 | 14 | 46 |      37.0 |   0.063 |      248 |         37.0 |      0.031 |         225 |                       |

Large (20–30+ vertices)

|  id |  V |   E | Prim cost | Prim ms | Prim ops | Kruskal cost | Kruskal ms | Kruskal ops |
| --: | -: | --: | --------: | ------: | -------: | -----------: | ---------: | ----------: |
| 201 | 22 |  42 |      93.0 |   1.726 |      252 |         93.0 |      1.108 |         286 |
| 202 | 26 |  25 |     143.0 |   0.069 |       50 |        143.0 |      0.064 |         245 |
| 203 | 24 |  67 |      65.0 |   0.122 |      383 |         65.0 |      0.114 |         371 |
| 204 | 28 | 103 |      77.0 |   0.190 |      631 |         77.0 |      0.083 |         490 |

Notes

* For every connected graph, Prim and Kruskal returned the same MST cost.
* Disconnected cases (small #5, medium #104) were correctly reported as no MST.

2. Comparison: theory vs. what happened here

Theory:

* Kruskal sorts edges and uses union–find to skip cycles. Time about O(E log E) (≈ O(E log V)).
* Prim grows one component using a priority queue. With a binary heap it is O(E log V); with a better decrease-key structure it can approach O(E + V log V).

Practice on these datasets:

* Runtime: Kruskal was faster on average in all three sets (see the dataset summary table). Overall mean time 0.1213 ms for Kruskal vs 0.1880 ms for Prim across 13 connected graphs.
* Operation counts: Prim often executed fewer counted operations (small: 25.2 vs 38.6; medium: 158 vs 173; large: 329 vs 348) but still ran slower, which suggests the per-operation cost on the heap/queue and cache behavior dominate more than the raw count.
* Near-tree case large #202 (E close to V): essentially a tie on time (0.069 vs 0.064 ms).
* Heaviest case large #204: Kruskal won clearly in both time and ops (0.083 vs 0.190 ms; 490 vs 631).
* Slowest run overall large #201: Kruskal still ahead (1.108 vs 1.726 ms).

3. Conclusion:

Both algorithms are correct and return the same MST cost whenever the graph is connected. With these inputs, Kruskal is the safer default if the graph comes as an edge list or you batch-build an MST once: it was faster on average in small, medium, and large sets. Prim remains attractive when you already have an adjacency structure and plan to grow a tree from a seed, but with a plain binary heap it did not beat Kruskal here. Expect Prim to improve on dense graphs if you switch to a stronger decrease-key structure; otherwise, Kruskal’s one-time sort plus simple union–find tends to be faster in practice.
