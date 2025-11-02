package io;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.io.GraphIO;
import smart.scheduling.common.model.DirectedGraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;


public class GraphIOTest {


    @Test
    void readInput_and_convertToGraph_ok() throws IOException {
        String json = """
    {
    "directed": true,
    "n": 4,
    "edges": [
    {"u":0,"v":1,"w":2},
    {"u":1,"v":2,"w":3},
    {"u":2,"v":3,"w":4}
    ],
    "source": 0,
    "weight_model": "edge"
    }
    """;
        Path tmp = Files.createTempFile("graph-", ".json");
        Files.writeString(tmp, json);
        GraphIO.InputData data = GraphIO.readInput(tmp);
        assertTrue(data.directed);
        assertEquals(4, data.n);
        assertEquals(3, data.edges.size());
        assertEquals(0, data.source);
        assertEquals("edge", data.weight_model);


        DirectedGraph g = GraphIO.toGraph(data);
        assertEquals(4, g.n());
        assertEquals(1, g.neighbors(0).size());
        assertEquals(1, g.neighbors(1).size());
    }
}
