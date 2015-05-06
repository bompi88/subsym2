package project5;

import algorithms.ea.Evolution;
import algorithms.ea.adultselection.AdultSelection;
import algorithms.ea.individual.operators.crossover.OneCrossover;
import algorithms.ea.individual.operators.mutation.MaybeMutator;
import algorithms.ea.mating.ParentSelection;
import algorithms.eann.IndividualBrain;
import gameworlds.flatland.Flatland;
import gameworlds.flatland.Movement;
import gameworlds.flatland.sensor.Sensed;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AIController {

    public static int globalScenariosToRun = 1;
    public static boolean globalIsStatic = false;


    private List<IndividualBrain> population;
    private int populationSize = 100;
    private int epoch = 0;
    private Evolution evolution;

    // 1. Make a population
    // 2. Run population on game world
    // 3. Collect fitness underway


    private Flatland currentScenario;

    public AIController() {
        evolution = new Evolution<>(IndividualBrain.class);
        evolution = evolution
                .setMatingStrategy(ParentSelection.SIGMA_SCALING)
                .setAdultSelectionsStrategy(AdultSelection.OVER_PRODUCTION)
                .setMutation(new MaybeMutator(0.1))
                .setCrossover(new OneCrossover())
                .setCHIlDREN_POOL_SIZE(200)
                .setPARENT_POOL_SIZE(110)
                .build();

        // ----------------------------------
        // Create random population
        // ----------------------------------
        population = new LinkedList<>();
        IntStream.range(0, populationSize)
                .forEach(i -> population.add(new IndividualBrain()));

        currentScenario = new Flatland(10, 1/3.0, 1/3.0);

        calculateFitnessOnPopulation();
    }


    private void calculateFitnessOnPopulation(){
        if(globalIsStatic)
            currentScenario = new Flatland(currentScenario);
        else
            currentScenario = new Flatland(10, 1/3.0, 1/3.0);

        population
                .stream()
                // Make phenotypes - In case they have changed since evolution
                .peek(IndividualBrain::buildPhenotypes)
                .peek(IndividualBrain::rewireBrain)
                // ----------------------------------
                // Fitness function that run Flatland
                // ----------------------------------
                .peek(individualBrain -> {
                    double totalFitness = 0;
                    for (int x = 0; x < globalScenariosToRun; x++) {
                        // Setup Flatland
                        Flatland flatland = new Flatland(currentScenario);

                        // Run Flatland with 60 iteration
                        for (int i = 0; i < 60; i++) {
                            Movement move = helperIndividualFindMove(flatland, individualBrain);

                            // Perform the move
                            flatland.move(move);
                        }
                        totalFitness += flatland.getStats();
                    }

                    // Record the fitness
                    individualBrain.setFitness(totalFitness / globalScenariosToRun);
                })
                .collect(Collectors.toList());
    }


    public static Movement helperIndividualFindMove(Flatland flatland, IndividualBrain individualBrain){
        Sensed sensoryInput = flatland.getSensory();
        List<Double> input = new LinkedList<>();
        input.add((double) sensoryInput.left.value);
        input.add((double) sensoryInput.front.value);
        input.add((double) sensoryInput.right.value);


        // Run through brain
        List<Double> output = individualBrain.think(input);

        // Find out what to do with output
        Map<Integer, Double> list = new HashMap<>();
        list.put(0, output.get(0));
        list.put(1, output.get(1));
        list.put(2, output.get(2));

        Optional<Map.Entry<Integer, Double>> val = list.entrySet().stream()
                .sorted(byValue.reversed())
                .findFirst();

        switch (val.get().getKey()) {
            case 0:
                return Movement.LEFTFORWARD;
            case 1:
                return Movement.FORWARD;
            case 2:
                return Movement.RIGHTFORWARD;
            default:
                System.err.println("WTF");
                return Movement.FORWARD;
        }
    }

    /**
     * Runs one epoch and returns the best individual
     */
    public IndividualBrain runOneEpoch(){

        // Run population through EANN
        population = evolution.nextEpoch(population, epoch++);

        // Calculate the fitness
        calculateFitnessOnPopulation();

        return population.stream().max(IndividualBrain::compareTo).get();
    }

    static Comparator<Map.Entry<Integer, Double>> byValue = (entry1, entry2) -> entry1.getValue().compareTo(
            entry2.getValue());

    public List<IndividualBrain> getPopulation() {
        return population;
    }

    public Flatland getCurrentScenario() {
        return currentScenario;
    }

    public int getEpoch() {
        return epoch;
    }
}
