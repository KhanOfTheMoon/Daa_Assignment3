package org.example;

import java.util.*;

public class Graph {
    public static class Edge {
        public final String u, v;
        public final double w;
        public Edge(String u, String v, double w) { this.u = u; this.v = v; this.w = w; }
        public String other(String x) { return u.equals(x) ? v : (v.equals(x) ? u : null); }
    }

    private final Set<String> vertices = new LinkedHashSet<>();
    private final List<Edge> edges = new ArrayList<>();
    private Map<String, List<Edge>> adj = null;

    public Set<String> getVertices() { return vertices; }
    public List<Edge> getEdges() { return edges; }

    public void addEdge(String u, String v, double w) {
        vertices.add(u); vertices.add(v);
        edges.add(new Edge(u, v, w));
        adj = null;
    }

    public Map<String, List<Edge>> adj() {
        if (adj == null) {
            adj = new LinkedHashMap<>();
            for (String v : vertices) adj.put(v, new ArrayList<>());
            for (Edge e : edges) { adj.get(e.u).add(e); adj.get(e.v).add(e); }
        }
        return adj;
    }

    public static Graph fromInput(JsonInput.InputGraph in) {
        Graph g = new Graph();
        if (in.vertices != null) g.vertices.addAll(in.vertices); // внутри класса доступ к private полю есть
        if (in.edges != null) {
            for (JsonInput.EdgeDto e : in.edges) g.addEdge(e.from, e.to, e.weight);
        }
        return g;
    }
}
