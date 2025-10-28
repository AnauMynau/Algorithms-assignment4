package com.smart.scheduling.graph.model;

public record Vertex(String id, String name, int duration, VertexType type) {
    public enum VertexType { TASK, ANALYTICS, MAINTENANCE }
}
