package smart.scheduling.common.model;

import java.util.*;

public interface Graph {
    int n();
    List<Edge> neighbors(int u);
}