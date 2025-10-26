Report — Optimization of a City Transportation Network (MST)

1. Summary of input data and per-case results

Small (4–6 vertices):

| id |  V |  E | Prim cost | Prim ms | Prim ops | Kruskal cost | Kruskal ms | Kruskal ops | note                  |
| -: | -: | -: | --------: | ------: | -------: | -----------: | ---------: | ----------: | :-------------------- |
|  1 |  5 |  4 |      14.0 |   0.027 |        8 |         14.0 |      0.021 |          29 |                       |
|  2 |  5 |  7 |      15.0 |   0.035 |       24 |         15.0 |      0.016 |          45 |                       |
|  3 |  4 |  6 |       9.0 |   0.011 |       19 |          9.0 |      0.013 |          20 |                       |
|  4 |  6 | 13 |      10.0 |   0.020 |       50 |         10.0 |      0.028 |          60 |                       |
|  5 |  6 |  3 |         — |   0.009 |        4 |            — |      0.010 |          21 | disconnected (no MST) |
|  6 |  5 |  7 |      16.0 |   0.012 |       25 |         16.0 |      0.011 |          39 |                       |

Medium (10–15 vertices):

|  id |  V |  E | Prim cost | Prim ms | Prim ops | Kruskal cost | Kruskal ms | Kruskal ops | note                  |
| --: | -: | -: | --------: | ------: | -------: | -----------: | ---------: | ----------: | :-------------------- |
| 101 | 10 | 17 |      33.0 |   0.069 |       71 |         33.0 |      0.043 |         106 |                       |
| 102 | 12 | 22 |      40.0 |   0.067 |      106 |         40.0 |      0.055 |         159 |                       |
| 103 | 15 | 40 |      39.0 |   0.085 |      207 |         39.0 |      0.046 |         202 |                       |
| 104 | 11 |  4 |         — |   0.015 |        4 |            — |      0.017 |          28 | disconnected (no MST) |
| 105 | 14 | 46 |      37.0 |   0.080 |      248 |         37.0 |      0.048 |         225 |                       |

Large (20–30+ vertices):

|  id |  V |   E | Prim cost | Prim ms | Prim ops | Kruskal cost | Kruskal ms | Kruskal ops |
| --: | -: | --: | --------: | ------: | -------: | -----------: | ---------: | ----------: |
| 201 | 22 |  42 |      93.0 |   2.219 |      252 |         93.0 |      1.637 |         286 |
| 202 | 26 |  25 |     143.0 |   0.063 |       50 |        143.0 |      0.062 |         245 |
| 203 | 24 |  67 |      65.0 |   0.154 |      383 |         65.0 |      0.094 |         371 |
| 204 | 28 | 103 |      77.0 |   0.217 |      631 |         77.0 |      0.130 |         490 |

Per-dataset averages (connected graphs only):

| dataset | avg Prim ms | avg Kruskal ms | avg Prim ops | avg Kruskal ops |
| ------- | ----------: | -------------: | -----------: | --------------: |
| Small   |      0.0210 |         0.0178 |         25.2 |            38.6 |
| Medium  |      0.0753 |         0.0480 |          158 |             173 |
| Large   |      0.6633 |         0.4808 |          329 |             348 |

Notes:

* For every connected graph, MST total cost was identical for Prim and Kruskal.
* Disconnected graphs were correctly reported as having no MST.

2. Comparison of Prim and Kruskal (theory and in practice)

In theory:

* Kruskal sorts all edges and adds them if they do not form a cycle, using union–find. Typical time: O(E log E) (about O(E log V)).
* Prim grows the tree from a seed using a priority queue on edge weights. With a binary heap, time is O(E log V); with stronger decrease-key structures, it can approach O(E + V log V).

In practice:

* Runtime: Kruskal was faster on average across small, medium, and large sets (see averages table).
* Operation counts: Prim often performed fewer counted operations, but that did not consistently reduce runtime because the cost of one counted operation differs between the algorithms (priority-queue operations vs sorting plus union–find).
* Near-tree graphs (E close to V−1) showed almost equal runtimes.
* Denser graphs still favored Kruskal in this implementation.

3. Conclusion:

Both algorithms reliably produce the same MST cost on connected graphs; the difference is in workflow and speed. If your data is an edge list and you want a simple one-shot build, Kruskal is the most straightforward and, in these runs, usually the faster choice. If you access the graph via adjacency and want to grow the tree from a starting district, Prim feels more natural and remains competitive; with a stronger decrease-key strategy it can overtake on denser networks. On sparse or near-tree inputs, their runtimes are typically close. For a default in batch processing, pick Kruskal and keep Prim available for adjacency-driven or incremental scenarios, upgrading Prim’s priority queue if dense areas become a bottleneck.
