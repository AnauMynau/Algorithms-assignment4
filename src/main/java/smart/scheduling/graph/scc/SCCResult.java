package smart.scheduling.graph.scc;

import java.util.List;


public final class SCCResult {
    public final List<List<Integer>> components; // each comp: list of vertices
    public final int[] compIdByVertex; // vertex -> component id


    public SCCResult(List<List<Integer>> components, int[] compIdByVertex) {
        this.components = components; this.compIdByVertex = compIdByVertex;
    }
}
