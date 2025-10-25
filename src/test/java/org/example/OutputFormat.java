package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class OutputFormat {

    @Test
    void outputFiles() throws Exception {
        String input = """
        {
          "graphs": [
            {"graph_id":1,"vertices":["A","B","C"],"edges":[
              {"from":"A","to":"B","weight":1},
              {"from":"B","to":"C","weight":2},
              {"from":"A","to":"C","weight":3}
            ]},
            {"graph_id":2,"vertices":["X","Y","Z","W"],"edges":[
              {"from":"X","to":"Y","weight":5},
              {"from":"Z","to":"W","weight":1}
            ]}
          ]
        }""";

        Path dir = Files.createTempDirectory("mst_app_test");
        Path in  = dir.resolve("in.json");
        Path out = dir.resolve("out.json");
        Path csv = dir.resolve("out.csv");
        Files.writeString(in, input);

        Starter.main(new String[]{ in.toString(), out.toString(), csv.toString() });

        assertTrue(Files.exists(out));
        assertTrue(Files.exists(csv));

        ObjectMapper m = new ObjectMapper();
        JsonNode root = m.readTree(Files.readString(out));
        assertTrue(root.has("results"));
        assertEquals(2, root.get("results").size());

        for (JsonNode r : root.get("results")) {
            assertTrue(r.has("graph_id"));
            assertTrue(r.get("input_stats").has("vertices"));
            assertTrue(r.get("input_stats").has("edges"));

            JsonNode prim = r.get("prim");
            JsonNode kr   = r.get("kruskal");
            assertTrue(prim.has("operations_count"));
            assertTrue(kr.has("operations_count"));
            assertTrue(prim.has("execution_time_ms"));
            assertTrue(kr.has("execution_time_ms"));

            if (!prim.get("total_cost").isNull() && !kr.get("total_cost").isNull()) {
                assertEquals(prim.get("total_cost").asDouble(),
                        kr.get("total_cost").asDouble(), 1e-9);
            }
        }
    }
}
