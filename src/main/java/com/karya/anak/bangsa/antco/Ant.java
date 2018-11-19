package com.karya.anak.bangsa.antco;

import java.util.concurrent.atomic.AtomicInteger;

class Ant {
    private int path[];
    private boolean visited[];
    private AtomicInteger currentIndex;

    Ant(int numCities, AtomicInteger currentIndex) {
        this.path = new int[numCities];
        this.visited = new boolean[numCities];
        this.currentIndex = currentIndex;
    }

    void visitCity(int city) {
        path[currentIndex.get() + 1] = city;
        visited[city] = true;
    }

    int currentCity() {
        return path[currentIndex.get()];
    }

    int[] getTour() {
        return path;
    }

    boolean visited(int city) {
        return visited[city];
    }

    double tourLength() {
        double[][] graph = Main.getGraph();
        double length = graph[path[graph.length - 1]][path[0]];
        for (int i = 0; i < graph.length - 1; i++) {
            length += graph[path[i]][path[i + 1]];
        }
        return length;
    }

    void clear() {
        for (int i = 0; i < visited.length; i++)
            visited[i] = false;
    }
}
