Report — Optimization of a City Transportation Network (MST)

1. Summary of input data and per-case results

Dataset summary:

| dataset | graphs_connected | avg_prim_ms | avg_kruskal_ms | avg_prim_ops | avg_kruskal_ops | kruskal_faster | all_equal_cost |
| ------- | ---------------: | ----------: | -------------: | -----------: | --------------: | -------------: | -------------: |
| large   |                4 |      0.5269 |         0.3421 |        320.0 |           348.0 |           true |           true |
| medium  |                4 |      0.0601 |         0.0338 |        158.0 |           173.0 |           true |          false |
| small   |                5 |      0.0190 |         0.0146 |         25.2 |            38.6 |           true |          false |
| OVERALL |               13 |      0.1879 |         0.1213 |        159.5 |           175.2 |           true |          false |

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

Both algorithms always produced the same MST cost on every connected graph (13 of 15 total; the two disconnected cases were small #5 and medium #104, both correctly reported as no MST). On average runtime, Kruskal was faster in all three datasets: small 0.0178 ms vs 0.0210 ms, medium 0.0480 ms vs 0.0753 ms, large 0.4808 ms vs 0.6633 ms (Kruskal vs Prim). Prim often used fewer counted operations overall (small 25.2 vs 38.6, medium 158 vs 173), but that didn’t consistently translate into lower time; on large graphs the gap narrowed (329 vs 348). There were clear exceptions: Prim actually beat Kruskal on small #4 (0.020 ms vs 0.028 ms), and the near-tree case large #202 was essentially a tie (0.063 ms vs 0.062 ms) while Kruskal did many more ops due to sorting (245 vs 50). The densest/heaviest case large #204 favored Kruskal both in time and ops (0.130 ms vs 0.217 ms; 490 vs 631), and the slowest run overall was large #201 where Kruskal still led (1.637 ms vs 2.219 ms). In practice, use Kruskal by default for edge-list inputs and batch builds, keep Prim for adjacency-driven or incremental growth, and expect Prim to benefit from a stronger decrease-key strategy if you target very dense networks.
