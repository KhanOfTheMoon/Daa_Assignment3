package org.example;

import com.fasterxml.jackson.databind.*;
import java.io.File;
import java.util.*;

public class JsonInput {
    public static class InputData { public List<InputGraph> graphs = new ArrayList<>(); }
    public static class InputGraph {
        public int graph_id;                 // заполним из id или graph_id
        public List<String> vertices;        // заполним из nodes или vertices
        public List<EdgeDto> edges;
    }
    public static class EdgeDto { public String from, to; public double weight; }

    public static InputData read(String path) throws Exception {
        ObjectMapper m = new ObjectMapper();
        JsonNode root = m.readTree(new File(path));
        InputData out = new InputData();
        for (JsonNode g : root.get("graphs")) {
            InputGraph ig = new InputGraph();
            ig.graph_id = g.has("graph_id") ? g.get("graph_id").asInt() : g.get("id").asInt();
            JsonNode verts = g.has("vertices") ? g.get("vertices") : g.get("nodes");
            ig.vertices = new ArrayList<>();
            for (JsonNode v : verts) ig.vertices.add(v.asText());
            ig.edges = new ArrayList<>();
            for (JsonNode e : g.get("edges")) {
                EdgeDto ed = new EdgeDto();
                ed.from = e.get("from").asText();
                ed.to = e.get("to").asText();
                ed.weight = e.get("weight").asDouble();
                ig.edges.add(ed);
            }
            out.graphs.add(ig);
        }
        return out;
    }
}
