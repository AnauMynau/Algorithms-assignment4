package io;

import org.junit.jupiter.api.Test;
import smart.scheduling.common.io.GraphIO;
import smart.scheduling.common.model.DirectedGraph;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.junit.jupiter.api.Assertions.*;


public class InputValidationTest {


    @Test
    void nonDirectedInput_throws() throws IOException {
        String json = """
{"directed": false, "n": 2, "edges": [{"u":0,"v":1,"w":1}]}
""";
        Path tmp = Files.createTempFile("g-", ".json");
        Files.writeString(tmp, json);
        GraphIO.InputData d = GraphIO.readInput(tmp);
        assertThrows(IllegalArgumentException.class, () -> GraphIO.toGraph(d));
    }


    @Test
    void outOfRangeEdge_throwsOnAdd() {
        DirectedGraph g = new DirectedGraph(2);
        assertThrows(IndexOutOfBoundsException.class, () -> g.addEdge(0, 5, 1));
    }
}
