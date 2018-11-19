package com.karya.anak.bangsa.antco;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static com.karya.anak.bangsa.antco.Math.pow;

public class Main {

    private static final double c = 1.0;
    private static final double alpha = 1;
    private static final double beta = 5;
    private static final double evaporation = 0.5;
    private static final double Q = 500;
    private static final double numAntFactor = 0.8;
    // probability of pure random selection of the next town
    private static final double pr = 0.01;

    private static final int maxIterations = 2000;

    private int numCities = 0;
    private int numAnts = 0;

    static double[][] getGraph() {
        return graph;
    }

    private void setGraph(double[][] graph) {
        Main.graph = graph;
    }

    private static double[][] graph = null;
    private double trails[][] = null;
    private Ant ants[] = null;
    private Random rand = new Random();
    private double probs[] = null;

    private AtomicInteger currentIndex = new AtomicInteger(0);

    private int[] bestTour;
    private double bestTourLength;

    public static void main(String[] args) {
        // Load in TSP data file.
        if (args.length < 1) {
            System.err.println("Please specify a TSP data file.");
            return;
        }
        Main antTsp = new Main();
        try {
            antTsp.readGraph(args[0]);
        } catch (IOException e) {
            System.err.println("Error reading graph.");
            return;
        }

        // Repeatedly solve - will keep the best tour found.
        do {
            antTsp.solve();
        } while (true);

    }

    private void readGraph(String path) throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader buf = new BufferedReader(fr);
        String line;
        int i = 0;

        while ((line = buf.readLine()) != null) {
            String splitA[] = line.split(" ");
            //Make linkedlist of line
            LinkedList<String> split = new LinkedList<String>();
            for (String s : splitA)
                if (!s.isEmpty())
                    split.add(s);

            if (graph == null)
                graph = new double[split.size()][split.size()];
            int j = 0;

            for (String s : split)
                if (!s.isEmpty())
                    graph[i][j++] = Double.parseDouble(s) + 1;
            i++;
        }
        setGraph(graph);
        numCities = graph.length;
        // number of ants used = numCities*numAntFactor
        numAnts = (int) (numCities * numAntFactor);

        // all memory allocations done here
        trails = new double[numCities][numCities];
        probs = new double[numCities];
        ants = new Ant[numAnts];
        for (int j = 0; j < numAnts; j++)
            ants[j] = new Ant(graph.length, currentIndex);
    }

    private void solve() {
        for (int i = 0; i < numCities; i++)
            for (int j = 0; j < numCities; j++)
                trails[i][j] = c;

        int iteration = 0;
        while (iteration < maxIterations) {
            setupAnts();
            moveAnts();
            updateTrails();
            updateBest();
            iteration++;
        }

        System.out.println("Best tour length: " + (bestTourLength - numCities));
        System.out.println("Best tour:" + tourToString(bestTour));
        bestTour.clone();
    }

    // numAnts ants with random start city
    private void setupAnts() {
        currentIndex.set(-1);
        for (int i = 0; i < numAnts; i++) {
            ants[i].clear();
            // faster than fresh allocations.
            ants[i].visitCity(rand.nextInt(numCities));
        }
        currentIndex.incrementAndGet();
    }

    // Choose the next town for all ants
    private void moveAnts() {
        while (currentIndex.get() < numCities - 1) {
            for (Ant a : ants)
                a.visitCity(selectNextTown(a));
            currentIndex.incrementAndGet();
        }
    }

    private int selectNextTown(Ant ant) {
        // sometimes just randomly select
        if (rand.nextDouble() < pr) {
            int t = rand.nextInt(numCities - currentIndex.get());
            // random town
            int j = -1;
            for (int i = 0; i < numCities; i++) {
                if (!ant.visited(i))
                    j++;
                if (j == t)
                    return i;
            }
        }
        // calculate probabilities for each town (stored in probs)
        probTo(ant);
        // randomly select according to probs
        double r = rand.nextDouble();
        double tot = 0;
        for (int i = 0; i < numCities; i++) {
            tot += probs[i];
            if (tot >= r)
                return i;
        }
        throw new RuntimeException("Not supposed to get here.");
    }

    private void probTo(Ant ant) {
        int currentCity = ant.currentCity();

        double denom = 0.0;
        for (int l = 0; l < numCities; l++)
            if (!ant.visited(l))
                denom += pow(trails[currentCity][l], alpha)
                        * pow(1.0 / graph[currentCity][l], beta);


        for (int nextCity = 0; nextCity < numCities; nextCity++) {
            if (ant.visited(nextCity)) {
                probs[nextCity] = 0.0;
            } else {
                double numerator = pow(trails[currentCity][nextCity], alpha)
                        * pow(1.0 / graph[currentCity][nextCity], beta);
                probs[nextCity] = numerator / denom;
            }
        }
    }

    // Update trails based on ants paths
    private void updateTrails() {
        // evaporation
        for (int i = 0; i < numCities; i++)
            for (int j = 0; j < numCities; j++)
                trails[i][j] *= evaporation;

        // each ants contribution
        for (Ant ant : ants) {
            double contribution = Q / ant.tourLength();
            for (int i = 0; i < numCities - 1; i++) {
                trails[ant.getTour()[i]][ant.getTour()[i + 1]] += contribution;
            }
            trails[ant.getTour()[numCities - 1]][ant.getTour()[0]] += contribution;
        }
    }

    private void updateBest() {
        if (bestTour == null) {
            bestTour = ants[0].getTour();
            bestTourLength = ants[0].tourLength();
        }
        for (Ant a : ants) {
            if (a.tourLength() < bestTourLength) {
                bestTourLength = a.tourLength();
                bestTour = a.getTour().clone();
            }
        }
    }

    private static String tourToString(int tour[]) {
        StringBuilder t = new StringBuilder();
        for (int i : tour)
            t.append(" ").append(i);
        return t.toString();
    }
}
