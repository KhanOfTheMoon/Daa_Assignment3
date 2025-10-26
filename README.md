Report — Optimization of a City Transportation Network (MST)

1. Summary of input data and per-case results

Dataset summary (connected graphs only; averages)

| dataset         | graphs_connected | avg_prim_ms | avg_kruskal_ms | avg_prim_ops | avg_kruskal_ops | kruskal_faster | all_equal_cost |
| --------------- | ---------------: | ----------: | -------------: | -----------: | --------------: | :------------: | :------------: |
| assign_3_large  |                4 |       0.420 |          0.385 |        329.0 |           348.0 |      true      |      true      |
| assign_3_medium |                4 |      0.0725 |          0.045 |        158.0 |           173.0 |      true      |      false     |
| assign_3_small  |                5 |       0.018 |          0.014 |         25.2 |            38.6 |      true      |      false     |
| OVERALL         |               13 |      0.1585 |         0.1377 |        159.5 |           175.2 |      true      |      false     |

Small (4–6 vertices)

| id |  V |  E | Prim cost | Prim ms | Prim ops | Kruskal cost | Kruskal ms | Kruskal ops | note                  |
| -: | -: | -: | --------: | ------: | -------: | -----------: | ---------: | ----------: | --------------------- |
|  1 |  5 |  4 |      14.0 |    0.03 |        8 |         14.0 |       0.02 |          29 |                       |
|  2 |  5 |  7 |      15.0 |    0.02 |       24 |         15.0 |       0.01 |          45 |                       |
|  3 |  4 |  6 |       9.0 |    0.01 |       19 |          9.0 |       0.01 |          20 |                       |
|  4 |  6 | 13 |      10.0 |    0.02 |       50 |         10.0 |       0.02 |          60 |                       |
|  5 |  6 |  3 |         — |    0.01 |        4 |            — |       0.01 |          21 | disconnected (no MST) |
|  6 |  5 |  7 |      16.0 |    0.01 |       25 |         16.0 |       0.01 |          39 |                       |

Medium (10–15 vertices)

|  id |  V |  E | Prim cost | Prim ms | Prim ops | Kruskal cost | Kruskal ms | Kruskal ops | note                  |
| --: | -: | -: | --------: | ------: | -------: | -----------: | ---------: | ----------: | --------------------- |
| 101 | 10 | 17 |      33.0 |    0.08 |       71 |         33.0 |       0.05 |         106 |                       |
| 102 | 12 | 22 |      40.0 |    0.09 |      106 |         40.0 |       0.04 |         159 |                       |
| 103 | 15 | 40 |      39.0 |    0.07 |      207 |         39.0 |       0.05 |         202 |                       |
| 104 | 11 |  4 |         — |    0.02 |        4 |            — |       0.02 |          28 | disconnected (no MST) |
| 105 | 14 | 46 |      37.0 |    0.05 |      248 |         37.0 |       0.04 |         225 |                       |

Large (20–30+ vertices)

|  id |  V |   E | Prim cost | Prim ms | Prim ops | Kruskal cost | Kruskal ms | Kruskal ops |
| --: | -: | --: | --------: | ------: | -------: | -----------: | ---------: | ----------: |
| 201 | 22 |  42 |      93.0 |    1.40 |      252 |         93.0 |       1.36 |         286 |
| 202 | 26 |  25 |     143.0 |    0.04 |       50 |        143.0 |       0.04 |         245 |
| 203 | 24 |  67 |      65.0 |    0.10 |      383 |         65.0 |       0.06 |         371 |
| 204 | 28 | 103 |      77.0 |    0.14 |      631 |         77.0 |       0.08 |         490 |

Notes:

* For every connected graph, both algorithms returned the same MST cost.
* Disconnected cases (small #5 and medium #104) were correctly reported as having no MST.

2. Comparison:

In theory:

* Kruskal: sort edges by weight, add if it doesn’t create a cycle (union–find). Time about O(E log E) ≈ O(E log V).
* Prim: grow a tree from a seed using a priority queue of edges. With a binary heap it is O(E log V); with better decrease-key structures it can approach O(E + V log V).

In practice:

* Average runtime: Kruskal was faster in all three sets. Averages were small 0.014 ms vs 0.018 ms, medium 0.045 ms vs 0.0725 ms, large 0.385 ms vs 0.420 ms (Kruskal vs Prim). Overall across 13 connected graphs: 0.1377 ms vs 0.1585 ms.
* Operation counts: Prim used fewer counted ops on average (small 25.2 vs 38.6; medium 158 vs 173; large 329 vs 348), but that did not make it faster here. The cost of individual operations (heap updates vs sorting and union–find) and memory behavior dominated.
* Edge-sparse near-tree: large #202 (E close to V) was essentially a tie on time (0.04 vs 0.04), which is typical when there’s little choice among edges.
* Heaviest case: large #204 favored Kruskal strongly in both time and ops (0.08 vs 0.14 ms; 490 vs 631).
* Slowest case: large #201 still had Kruskal slightly ahead (1.36 vs 1.40 ms).

3. Conclusion:

Kruskal is a solid default when the graph comes as an edge list and you build an MST once; on these inputs it was faster in every dataset while producing the same MST cost. Prim remains a good choice when the graph is naturally accessed through adjacency and you grow from a seed; however, with a standard binary heap it did not surpass Kruskal here. Expect Prim to benefit on dense graphs if you use a decrease-key–friendly structure (e.g., pairing/Fibonacci-style heaps). For sparse and near-tree graphs both algorithms converge in cost and are close in time; for denser or more irregular graphs, Kruskal’s one-time sort plus straightforward union–find kept a small but consistent edge in these runs.
