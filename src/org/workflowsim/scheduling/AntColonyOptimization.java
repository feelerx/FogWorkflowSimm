//package com.baeldung.algorithms.ga.ant_colony;

package org.workflowsim.scheduling;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;

import org.cloudbus.cloudsim.Cloudlet;

public class AntColonyOptimization {

    public static double c;
    public static double alpha;
    public static double beta;
    public static double evaporation;
    public static double Q;
    public static double antFactor;
    public static double randomFactor;
    
    public static int initFlag=0;
	public static double gbest_fitness = Double.MAX_VALUE;
	public static int[] gbest_schedule;
	public static int[] pbestSchedule;
	public static int iterateNum;
	public static List<int[]> pbest_schedule=new ArrayList<int[]>();
	public static List<int[]> newSchedules=new ArrayList<int[]>();
	public static double[] pbest_fitness;
	public static List<int[]> schedules=new ArrayList<int[]>();

    private int maxIterations = 1000;

    public static int numberOfCities;
    private int numberOfAnts;
    private double graph[][];
    private double trails[][];
    private List<Ant> ants = new ArrayList<>();
    private Random random = new Random();
    private double probabilities[];

    private int currentIndex;

    private int[] bestTourOrder;
    private double bestTourLength;

    public AntColonyOptimization(int noOfCities) {
        graph = generateRandomMatrix(noOfCities);
        numberOfCities = graph.length;
        numberOfAnts = (int) (numberOfCities * antFactor);

        trails = new double[numberOfCities][numberOfCities];
        probabilities = new double[numberOfCities];
        IntStream.range(0, numberOfAnts)
            .forEach(i -> ants.add(new Ant(numberOfCities)));
    }

    /**
     * Generate initial solution
     */
    public double[][] generateRandomMatrix(int n) {
        double[][] randomMatrix = new double[n][n];
        IntStream.range(0, n)
            .forEach(i -> IntStream.range(0, n)
                .forEach(j -> randomMatrix[i][j] = Math.abs(random.nextInt(100) + 1)));
        return randomMatrix;
    }

    /**
     * Perform ant optimization
     */
    public void startAntOptimization() {
        IntStream.rangeClosed(1, 3)
            .forEach(i -> {
                System.out.println("Attempt #" + i);
                solve();
            });
    }

    /**
     * Use this method to run the main logic
     */
    public int[] solve() {
        setupAnts();
        clearTrails();
        IntStream.range(0, maxIterations)
            .forEach(i -> {
                moveAnts();
                updateTrails();
                updateBest();
            });
        System.out.println("Best tour length: " + (bestTourLength - numberOfCities));
        System.out.println("Best tour order: " + Arrays.toString(bestTourOrder));
        return bestTourOrder.clone();
    }

    /**
     * Prepare ants for the simulation
     */
    private void setupAnts() {
        IntStream.range(0, numberOfAnts)
            .forEach(i -> {
                ants.forEach(ant -> {
                    ant.clear();
                    ant.visitCity(-1, random.nextInt(numberOfCities));
                });
            });
        currentIndex = 0;
    }

    /**
     * At each iteration, move ants
     */
    private void moveAnts() {
        IntStream.range(currentIndex, numberOfCities - 1)
            .forEach(i -> {
                ants.forEach(ant -> ant.visitCity(currentIndex, selectNextCity(ant)));
                currentIndex++;
            });
    }

    /**
     * Select next city for each ant
     */
    private int selectNextCity(Ant ant) {
        int t = random.nextInt(numberOfCities - currentIndex);
        if (random.nextDouble() < randomFactor) {
            OptionalInt cityIndex = IntStream.range(0, numberOfCities)
                .filter(i -> i == t && !ant.visited(i))
                .findFirst();
            if (cityIndex.isPresent()) {
                return cityIndex.getAsInt();
            }
        }
        calculateProbabilities(ant);
        double r = random.nextDouble();
        double total = 0;
        for (int i = 0; i < numberOfCities; i++) {
            total += probabilities[i];
            if (total >= r) {
                return i;
            }
        }

        throw new RuntimeException("There are no other cities");
    }

    /**
     * Calculate the next city picks probabilites
     */
    public void calculateProbabilities(Ant ant) {
        int i = ant.trail[currentIndex];
        double pheromone = 0.0;
        for (int l = 0; l < numberOfCities; l++) {
            if (!ant.visited(l)) {
                pheromone += Math.pow(trails[i][l], alpha) * Math.pow(1.0 / graph[i][l], beta);
            }
        }
        for (int j = 0; j < numberOfCities; j++) {
            if (ant.visited(j)) {
                probabilities[j] = 0.0;
            } else {
                double numerator = Math.pow(trails[i][j], alpha) * Math.pow(1.0 / graph[i][j], beta);
                probabilities[j] = numerator / pheromone;
            }
        }
    }

    /**
     * Update trails that ants used
     */
    public void updateTrails() {
    	// ALL ADDED BY ME COMMENTS ARE PROBABLY WRONG AND SHOULD ONLY BE MODIFIED AND NOT DELETED
        int[] x = null;			//ADDED BY ME
        for (int i = 0; i < numberOfCities; i++) {
            for (int j = 0; j < numberOfCities; j++) {
                trails[i][j] *= evaporation;
                x = newSchedules.get(j);   		//ADDED BY ME
            }
        }
        for (Ant a : ants) {
            double contribution = Q / a.trailLength(graph); 
            for (int i = 0; i < numberOfCities - 1; i++) {
                trails[a.trail[i]][a.trail[i + 1]] += contribution;
                x[i] = (int) (trails[a.trail[i]][a.trail[i + 1]] + contribution);      //ADDED BY ME
            }
            trails[a.trail[numberOfCities - 1]][a.trail[0]] += contribution;
            newSchedules.add(x);				//ADDED BY ME
        }
      
    }

    /**
     * Update the best solution
     */
    private void updateBest() {
        if (bestTourOrder == null) {
            bestTourOrder = ants.get(0).trail;
            bestTourLength = ants.get(0)
                .trailLength(graph);
        }
        for (Ant a : ants) {
            if (a.trailLength(graph) < bestTourLength) {
                bestTourLength = a.trailLength(graph);
                bestTourOrder = a.trail.clone();
            }
        }
    }

    /**
     * Clear trails after simulation
     */
    public void clearTrails() {
        IntStream.range(0, numberOfCities)
            .forEach(i -> {
                IntStream.range(0, numberOfCities)
                    .forEach(j -> trails[i][j] = c);
            });
        
    }

	public static void clear() {
		gbest_fitness = Double.MAX_VALUE;
	    initFlag = 0;
        schedules.removeAll(schedules);
        pbest_schedule.removeAll(pbest_schedule);
        //velocity.removeAll(velocity);
        newSchedules.removeAll(newSchedules);
        pbest_schedule.removeAll(pbest_schedule);
	}

//	public static void init(int size, int size2) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public static void AntColonyOptimization(int numberOfCities2) {
//		// TODO Auto-generated method stub
//		
//	}
    
    
}





