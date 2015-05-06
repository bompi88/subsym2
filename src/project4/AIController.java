package project4;

import algorithms.ea.Evolution;
import algorithms.ea.adultselection.AdultSelection;
import algorithms.ea.individual.operators.crossover.OneCrossover;
import algorithms.ea.individual.operators.mutation.MaybeMutator;
import algorithms.ea.mating.ParentSelection;
import algorithms.ectrnn.IndividualCTRBrain;
import gameworlds.tracker.Tracker;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AIController {

    private List<IndividualCTRBrain> population;
    private int populationSize = 100;
    private int epoch = 0;
    private Evolution evolution;

    Tracker.GameType gameType = Tracker.GameType.NORMAL;

    private Tracker tracker;

    // 1. Make a population
    // 2. Run population on game world
    // 3. Collect fitness underway

    public AIController() {

        reset();

    }

    public void reset() {
        updateVariables();
        epoch = 0;

        tracker = new Tracker(gameType);
        evolution = new Evolution<>(IndividualCTRBrain.class);
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
                .forEach(i -> population.add(new IndividualCTRBrain()));

        calculateFitnessOnPopulation();
    }

    private void calculateFitnessOnPopulation(){

        population
                .stream()
                // Make phenotypes - In case they have changed since evolution
                .peek(IndividualCTRBrain::buildPhenotypes)
                .peek(IndividualCTRBrain::rewireBrain)
                // ----------------------------------
                // Fitness function that run Tracker
                // ----------------------------------
                .peek(individualBrain -> {
                    // Setup a new Tracker
                    tracker = new Tracker(gameType);

                    // Run Tracker with 600 iteration
                    for (int i = 0; i < 600; i++) {
                        Tracker.Movement move = helperIndividualFindMove(tracker, individualBrain);

                        // Perform the move
                        tracker.newStep(move);
                    }

                    // Record the fitness
                    individualBrain.setFitness(tracker.getStats());
                })
                .collect(Collectors.toList());
    }



    public static Tracker.Movement helperIndividualFindMove(Tracker tracker, IndividualCTRBrain individualBrain){

        // Run through brain
        List<Double> output = individualBrain.think(tracker.getSensory());

        // Find out what to do with output
        Map<Integer, Double> list = new HashMap<>();

        for(int i = 0; i < IndividualCTRBrain.outputLayers; i++) {
            list.put(i, output.get(i));
        }

        Optional<Map.Entry<Integer, Double>> val = list.entrySet().stream()
                .sorted(byValue.reversed())
                .findFirst();

        switch (val.get().getKey()) {
            case 0:
                return Tracker.Movement.LEFT;
            case 1:
                return Tracker.Movement.RIGHT;
            case 2:
                return Tracker.Movement.PULLDOWN;
            default:
                System.err.println("WTF");
                return Tracker.Movement.LEFT;
        }
    }


    private void updateVariables(){
        switch (gameType) {
            case NORMAL:
                IndividualCTRBrain.inputLayers = 5;
                IndividualCTRBrain.outputLayers = 2;
                break;
            case NOWRAP:
                IndividualCTRBrain.inputLayers = 7;
                IndividualCTRBrain.outputLayers = 2;
                break;
            case PULLDOWN:
                IndividualCTRBrain.inputLayers = 5;
                IndividualCTRBrain.outputLayers = 3;
                break;
        }
    }

    /**
     * Runs one epoch and returns the best individual
     */
    public IndividualCTRBrain runOneEpoch(){

        // Run population through EANN
        population = evolution.nextEpoch(population, epoch++);

        // Calculate the fitness
        calculateFitnessOnPopulation();

        return population.stream().max(IndividualCTRBrain::compareTo).get();
    }

    static Comparator<Map.Entry<Integer, Double>> byValue = (entry1, entry2) -> entry1.getValue().compareTo(
            entry2.getValue());


    public List<IndividualCTRBrain> getPopulation() {
        return population;
    }

    public int getEpoch() {
        return epoch;
    }

    public void setTrackerGameType (Tracker.GameType gameType){
        this.gameType = gameType;
    }

    public Tracker.GameType getGameType() {
        return gameType;
    }
}
