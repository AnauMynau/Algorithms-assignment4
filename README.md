# 🧠 Assignment 4 — Smart City / Smart Campus Scheduling

**Course:** Design & Analysis of Algorithms  
**Topic:** SCC + Topological Sort + Shortest/Longest Paths in DAGs  

---

## 📌 Summary

This project implements a **complete graph-analysis pipeline** for scheduling tasks in a smart city/campus scenario:

- 🌀 **SCC detection** (Tarjan) → *Condensation DAG*
- 🔁 **Topological order** (Kahn) over the DAG
- 🧭 **Shortest & Longest paths** in the DAG (DP over topo order)
- 📊 **Metrics & CSV export** for analysis

Everything runs from **Main** and generates a compact report:  
`results/summary_metrics_lite.csv`

---

## 📁 Project Structure


```
src/
 └─ main/java/smart/scheduling/
    ├─ app/
    │   └─ Main.java                  # single entry point; batch runner + CSV export
    ├─ common/
    │   ├─ io/GraphIO.java            # JSON input parsing → DirectedGraph
    │   ├─ metrics/                   # Metrics interface + DefaultMetrics
    │   ├─ model/                     # DirectedGraph, Edge, Graph, WeightModel
    │   ├─ util/SummaryCsvLite.java   # concise CSV writer (time in fractional ms)
    │   └─ util/Preconditions.java
    └─ graph/
       ├─ scc/                        # TarjanSCC, SCCResult, CondensationGraphBuilder
       ├─ topo/                       # KahnTopologicalSort, TopologicalSort
       └─ dagsp/                      # DagShortestPath, DagLongestPath, PathResult
data/                                 # 9 JSON datasets (small/medium/large)
results/                              # summary_metrics_lite.csv (generated)
```
## ▶️ How to Run

```
# Build
mvn clean package

# Run all datasets (default small+medium+large globbing)
mvn -q exec:java -Dexec.mainClass=smart.scheduling.app.Main

# Run explicitly all JSONs in /data
mvn -q exec:java -Dexec.mainClass=smart.scheduling.app.Main -Dexec.args="--all"

# Run by pattern (Windows-safe glob handled inside Main)
mvn -q exec:java -Dexec.mainClass=smart.scheduling.app.Main -Dexec.args="--glob data/small_*.json"

# Run a list of files
mvn -q exec:java -Dexec.mainClass=smart.scheduling.app.Main -Dexec.args="data/small_2_dag.json data/large_2.json"

```
Output: results/summary_metrics_lite.csv


## 📚 Algorithms Implemented
### 1) 🔁 Strongly Connected Components — Tarjan

Discovers SCCs in O(V+E) using index/lowLink and a stack.

Output is used to compress cycles before planning.

Then we build condensation DAG (each SCC → single node).

### 2) 🧱 Topological Sort — Kahn

Runs on the condensation DAG (acyclic).

Queue of zero-in-degree nodes; each vertex pushed/popped once.

Complexity O(V+E); ideal for scheduling.

### 3) 🧭 Paths in DAG — Shortest / Longest

Over the topo order, classic DP:

Shortest: relax dist[v] = min(dist[u] + w).

Longest (critical path): max-DP (sign inversion idea).

Complexity O(V+E).

We use edge weights (unweighted → all 1).

## 📊 Metrics & CSV

Columns (concise):
```
graph,stage,n,edges,sccCount,topoLength,criticalPathLen,avgPathLen,timeMillis,density,speedPerEdge,notes
```

stage: SCC | Topo | Shortest | Longest

timeMillis: measured with System.nanoTime() → fractional ms

density: edges / (n·(n−1))

speedPerEdge: timeMillis / edges

Missing/irrelevant cells may appear as null (e.g., no path metrics on SCC/Topo).
Если нужно — можно заменить на NA прямо в SummaryCsvLite.esc(...).

Why some values are null?

SCC/Topo don’t produce path lengths → path columns are null.

Shortest doesn’t compute “criticalPathLen”.

If source can’t reach others (rare for our datasets), averages can be null.

speedPerEdge is null when timeMillis=0 or edges=0.

## 🧾 Datasets (9 total)

| Category | Nodes (n) | Description | Variants |
|-----------|------------|--------------|-----------|
| 🟩 **Small** | 6–10 | Simple DAGs / 1–2 cycles | 3 |
| 🟧 **Medium** | 10–20 | Mixed graphs, several SCCs | 3 |
| 🟥 **Large** | 20–50 | Performance & timing tests | 3 |


Design choices:

Both cyclic and acyclic graphs.

Different densities (sparse vs dense).

At least one case with multiple SCCs.

🔬 Analysis (student view)
SCC (Tarjan)

DFS visits ≈ n, edge scans ≈ m → linear, как в теории.

Время для small/medium графов — суб-миллисекунды.

Компрессия циклов сразу упрощает планирование (меньше вершин в DAG).

Topological Sort (Kahn)

Пуши/попы ≈ n.

Работает стабильно: на конденсации всегда ациклично → валидный порядок.

DAG Shortest / Longest

DP по топологическому порядку; O(V+E).

На более плотных графах больше релаксаций → немного растёт время (всё равно < 1 ms в наших данных).

Longest = критический путь (что нельзя задерживать).

Shortest показывает нижнюю границу длительности при текущей структуре.

Mini example of interpretation

Analysis (student view)

SCC (Tarjan)
  • DFS visits ≈ n, edge scans ≈ m → linear, as in theory.
  
  • Time for small/medium graphs — sub-milliseconds.
  
  • Cycle compression immediately simplifies planning (fewer vertices in DAG).
  
Topological Sort (Kahn)

  • Push/pop ≈ n.
  
  • Works stably: condensation is always acyclic → valid order.
  
DAG Shortest / Longest

  • DP by topological order; O(V+E).
  
  • More relaxations on denser graphs → time increases slightly (still < 1 ms in our data).
  
  • Longest = critical path (which cannot be delayed).
  
  • Shortest shows the lower bound of duration for the current structure.

### Mini example of interpretation

| Graph          |  n |   m | Density | Stage    | Result                         | Time (ms) |
| -------------- | -: | --: | ------: | -------- | ------------------------------ | --------: |
| small_1_cycle  |  6 |   5 |    0.17 | Longest  | `criticalPathLen = 6`          |     ~0.00 |
| medium_3_dense | 16 |  64 |    0.27 | Longest  | `criticalPathLen ≈ 5`          |     ~0.02 |
| large_3        | 40 | 100 |    0.06 | Shortest | `avgPathLen ≈ 0` (DAG trivial) |     ~0.03 |



Conclusion: O(V+E) asymptotic behaviour has been empirically confirmed. Even on large data sets, measurements take only milliseconds.



Assignment Check (self-audit)

SCC + Condensation ✅

Topological ordering ✅

DAG Shortest & Longest ✅

Metrics + nanoTime ✅

9 datasets (small/medium/large) ✅

Single-click run (Maven) ✅

CSV report with analysis ✅

Clean packages + tests + README ✅

✏️ Notes on Implementation Details

Windows-safe globbing: Main.glob() splits dir and mask (no * inside Path.of()).

Source mapping: if JSON provides source as a vertex index, we map it to SCC component id (srcComp) before DAG-SP to avoid out-of-range errors on reduced DAG.

Fractional time: SummaryCsvLite.millisFromNanos returns double ms (so tiny runs aren’t rounded to 0).

Example CSV   
```
graph,stage,n,edges,sccCount,topoLength,criticalPathLen,avgPathLen,timeMillis,density,speedPerEdge,notes
small_1_cycle.json,SCC,6,5,4,, , ,0.00,0.1667,,Tarjan SCC
small_1_cycle.json,Topo,4,2,4,4,, ,0.01,0.1667,0.005,Condensation + Kahn
small_1_cycle.json,Shortest,4,2,4,4,,2.6667,0.01,0.1667,0.005,DAG shortest paths
small_1_cycle.json,Longest,4,2,4,4,6.0,2.6667,0.01,0.1667,0.005,Critical path

```


Testing (short)
• Unit tests cover TarjanSCC, KahnTopologicalSort, DAG shortest.

• Small "manual" graphs + edge cases.

• For credit: tests consistently pass locally on JDK 17–25.


Author

Student: Inayatulla Noyan

Astana IT University - 2 course: Design & Analysis of Algorithms

Assignment: #4 — Smart City / Smart Campus Scheduling

Date: November 2025


