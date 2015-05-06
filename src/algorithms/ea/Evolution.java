package algorithms.ea;

import algorithms.ea.adultselection.*;
import algorithms.ea.individual.Genotype;
import algorithms.ea.individual.Individual;
import algorithms.ea.individual.operators.GeneticOperator;
import algorithms.ea.individual.operators.crossover.ICrossover;
import algorithms.ea.individual.operators.mutation.IMutation;
import algorithms.ea.mating.ParentSelection;
import math.Statistics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 1. A population of potential solutions (i.e. population) all represented in some low-level
 genotypic form such as binary vectors.

 2. A developmental method for converting the genotypes into phenotypes. For this general
 EA, a simple routine for converting a binary genotype into a list of integers will suffice.
 This can be extended for future homework modules.

 3. A fitness evaluation method that can be applied to all phenotypes. You will want to translate
 this a very modular component of your EA such that a wide variety of fitness functions
 can be experimented with

 4. All 3 of the basic protocols for adult selection described in the ea-appendices.pdf chapter
 of the lecture notes. (Full, over-production and mixing)

 5. todo A set (of at least 4) mechanisms (also described in ea-appendices.pdf) for parent/mate
 selection. These 4 must include fitness-proportionate, sigma-scaling, and tournament
 selection.

 6. The genetic operators of mutation and crossover. For this project, you only need to
 define them for binary genomes (i.e. bit vectors). But your system should be able to
 handle different representations for later problems.

 7. A basic evolutionary loop for running the EA through many generations of evolution

 8. A logging routine that shows various data while the EA is running. This can be as simple
 as printing to stdout/console. For each generation, this data should be logged: generation
 number, best fitness, mean fitness, standard deviation (SD) of fitness, phenotype of the
 best individual.

 9. A plotting routine that gives a visualization of an EA run. The plot should show how
 the data from the logging changes from generation to generation: Generations on the X
 axis, and then plot the best, mean and SD of the fitness. For this, you can either use an
 existing plotting library for your language, or just copy the logging data to a spreadsheet
 program like Excel and create the plots there.
 1
 *
 */
public class Evolution<T extends Individual> {
    // -------------------------------------
    // Variable setup
    // -------------------------------------
    private Random random;
    private Class<T> reference;

    // -------------------------------------
    // Algorithm tweaks
    // -------------------------------------
    private IMutation mutatation = null;
    private ICrossover crossover = null;

    private AdultSelection selectionsStrategy = AdultSelection.OVER_PRODUCTION;
    private ParentSelection matingStrategy = ParentSelection.FITNESS_PROPORTIONATE;


    int POOL_SIZE = 40;

    int PARENT_POOL_SIZE = 35;
    int CHIlDREN_POOL_SIZE = 40;

    int eliteism = 0;

    /**
     * Needs the generic class type as a paramter
     * so it can be made when mating.
     * @param reference
     */
    public Evolution(Class<T> reference){
        this.reference = reference;
        random = new Random();
    }

    /**
     * Survival shit..
     *
     * @param population
     * @param generation
     * @return
     */
    private List<T> performAdultSelection(List<T> population, int generation) {
        switch (selectionsStrategy) {
//            All adults from the previous generation are removed (i.e., die),
//            and all children gain free entrance to the adult pool. Thus, selection pressure on juveniles is completely
//            absent.
            case FULL:
                return population.stream()
                        .filter(i -> !(i.getAge() < generation))
                        .sorted()
                        .collect(Collectors.toList());

//            All previous adults die, but m (the maximum size of the adult pool) is smaller
//            than n (the number of children). Hence, the children must compete among themselves for the m adult
//            spots, so selection pressure is significant. This is also known as (�, ?) selection, where � and ? are sizes
//            of the adult and child pools, respectively.
            case MIXING:
                List<T> rofl = population.stream()
                        .filter(i -> !(i.getAge() < generation))
                //        .collect(Collectors.toList());
                //rofl = rofl.stream()
                        .sorted()
                        .collect(Collectors.toList());
                return rofl.stream()
                        .skip(Helper.toSkip(rofl.size(), PARENT_POOL_SIZE))
                        .collect(Collectors.toList());

                //return rofl;

//            The m adults from the previous generation do not die, so they and the n
//            children compete in a free-for-all for the m adult spots in the next generation. Here, selection pressure
//            on juveniles is extremely high, since they are competing with some of the best individuals that have
//            evolved so far, regardless of their age. This is also known as (� + ?) selection, where the plus indicates
//            the mixing of adults and children during competition.
            case OVER_PRODUCTION:
                return population.stream()
                        .sorted()
                        .skip(Helper.toSkip(population.size(), PARENT_POOL_SIZE))
                        .collect(Collectors.toList());
        }

        return null;
    }

    double T = 10;

    private List<T> performMating(List<T> matingPool){
        List<T> childrenPool = new LinkedList<>();
        List<IndiviualWrapper<T>> popSorted;

        switch (matingStrategy) {
            case FITNESS_PROPORTIONATE:
                double populationFitness = matingPool.stream()
                        .mapToDouble(Individual::getFitness)
                        .sum();

                popSorted = matingPool.stream()
                        .map(i -> new IndiviualWrapper<>(i, i.getFitness() / populationFitness))
                        .sorted()
                        .collect(Collectors.toList());

                do {
                    T found1 = findWheelSpin(popSorted, random.nextDouble());
                    T found2 = findWheelSpin(popSorted, random.nextDouble());

                    if(found1 == null || found2 == null || found1 == found2) {
                        continue;
                    }

                    childrenPool.addAll(reproduce(found1, found2));
                    // todo Produce two or more children?
                    // todo Empty after, so can't be choosen again?
                } while(childrenPool.size() < CHIlDREN_POOL_SIZE);

                break;
            case SIGMA_SCALING:

                double gMean = Statistics.populationMean(matingPool);
                double gSD = Statistics.standardDeviationPopulation(matingPool);
                popSorted = matingPool.stream()
                        .map(i -> new IndiviualWrapper<>(i, Statistics.sigmaScaling(i.getFitness(), gMean, gSD)))
                        .sorted()
                        .collect(Collectors.toList());

                double scaleFactor = popSorted.stream()
                        .mapToDouble(IndiviualWrapper::getFitness)
                        .sum();

                popSorted.stream()
                        .forEach(i -> i.fitness /= scaleFactor);

                do {
                    T found1 = findWheelSpin(popSorted, random.nextDouble());
                    T found2 = findWheelSpin(popSorted, random.nextDouble());

                    if(found1 == null || found2 == null || found1 == found2) {
                        continue;
                    }

                    childrenPool.addAll(reproduce(found1, found2));
                    // todo Produce two or more children?
                    // todo Empty after, so can't be choosen again?
                } while(childrenPool.size() < CHIlDREN_POOL_SIZE);
                break;
            case TOURNAMENT:
                double TOURNAMENT_EPSILON = 0.3;
                int TOURNAMENT_K = 10;

                if(matingPool.size() < TOURNAMENT_K) TOURNAMENT_K = matingPool.size() - 1;

                do {
                    List<T> pool1 = new LinkedList<>();
                    List<T> pool2 = new LinkedList<>();

                    do{
                        T choosenOne = matingPool.get(random.nextInt(matingPool.size()));
                        if(!pool1.contains(choosenOne)) pool1.add(choosenOne);
                    }while(pool1.size() < TOURNAMENT_K);

                    do{
                        T choosenOne = matingPool.get(random.nextInt(matingPool.size()));
                        if(!pool2.contains(choosenOne)) pool2.add(choosenOne);
                    }while(pool2.size() < TOURNAMENT_K);

                    pool1 = pool1.stream().sorted().collect(Collectors.toList());
                    pool2 = pool2.stream().sorted().collect(Collectors.toList());

                    T found1 = null;
                    T found2 = null;

                    for(int i = pool1.size() - 1; i >= 0; i--) {
                        if(i == 0) {
                            found1 = pool1.get(i);
                        } else if(random.nextDouble() < 1 - TOURNAMENT_EPSILON){
                            found1 = pool1.get(i);
                            break;
                        }
                    }
                    for(int i = pool2.size() - 1; i >= 0; i--) {
                        if(i == pool2.size() - 1) {
                            found2 = pool2.get(i);
                        } else if(random.nextDouble() < 1 - TOURNAMENT_EPSILON){
                            found2 = pool2.get(i);
                            break;
                        }
                    }
                    if(found1 == null || found2 == null || found1 == found2) {
                        continue;
                    }

                    childrenPool.addAll(reproduce(found1, found2));
                    // todo Produce two or more children?
                    // todo Empty after, so can't be choosen again?
                } while(childrenPool.size() < CHIlDREN_POOL_SIZE);
                break;
            case BOLTZMAN:
                // todo rewrite IndividualWrapper to HashMap<K, V>
                popSorted = matingPool.stream()
                        .map(i -> new IndiviualWrapper<>(i, Math.exp(i.getFitness() / T)))
                        .collect(Collectors.toList());
                final double generationTotal = popSorted.stream().mapToDouble(IndiviualWrapper::getFitness).sum();

                popSorted = popSorted.stream()
                        .map(i -> {
                            i.fitness = i.fitness / generationTotal;
                            return i;
                        })
                        .collect(Collectors.toList());

                double scaleF = popSorted.stream()
                        .mapToDouble(IndiviualWrapper::getFitness)
                        .sum();

                popSorted.stream()
                        .forEach(i -> i.fitness /= scaleF);

                do {
                    T found1 = findWheelSpin(popSorted, random.nextDouble());
                    T found2 = findWheelSpin(popSorted, random.nextDouble());

                    if(found1 == null || found2 == null || found1 == found2) {
                        continue;
                    }

                    childrenPool.addAll(reproduce(found1, found2));
                    // todo Produce two or more children?
                    // todo Empty after, so can't be choosen again?
                } while(childrenPool.size() < CHIlDREN_POOL_SIZE);

                // todo bug T is not reset?
                T -= Constants.EPSILON_OR_ERROR;
                break;
        }

        return childrenPool;

    }

    public T findWheelSpin(List<IndiviualWrapper<T>> individualWrappers, double wheelSpin) {
        double fitnessAccumulator = 0;

        for(int i = 0; i < individualWrappers.size(); i++) {
            fitnessAccumulator += individualWrappers.get(i).fitness;
            if(wheelSpin < fitnessAccumulator) {
                return individualWrappers.get(i).individual;
            }
        }

        return null;
    }

    /**
     * TODO write
     * @param individual1
     * @param individual2
     * @return
     */
    private List<T> reproduce(T individual1, T individual2) {
        Genotype genotype1 = individual1.getGenotypes();
        Genotype genotype2 = individual2.getGenotypes();

        return GeneticOperator.crossover(crossover, genotype1, genotype2).stream()
                .map(g -> {
                    T t = getInstanceOf();
                    t.setAge(1 + ((individual1.getAge() > individual2.getAge()) ? individual1.getAge() : individual2.getAge()));
                    t.setGenotypes(g);
                    return t;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calulate the populations next epoch.
     *
     * Remember to create their phenotypes and calculate their fitness
     * before running.
     *
     * @param population
     * @param generation
     * @return
     */
    public List<T> nextEpochBad(List<T> population, int generation) {
        List<T> pool = new ArrayList<>(population);

        // 1. Parent selection - Who get to mate of the survivors
        // mating does parent selection then mates the results.
        List<T> newChildren = performMating(population).stream()
                .peek(individual -> individual.mutate(mutatation))
                .collect(Collectors.toList());

        pool.addAll(newChildren);

        // todo eliteism

        // 2. Adult selection - The fight for a place! Survival
        List<T> newPopulation = performAdultSelection(pool, generation);

        // todo mutate for all, or only children
        return newPopulation;
    }

    public List<T> nextEpoch(List<T> population, int generation) {
        List<T> pool = new ArrayList<>(population);

        // 1. Parent selection - Who get to mate of the survivors
        List<T> newChildren = performMating(population).stream()
                .peek(individual -> individual.mutate(mutatation))
                .collect(Collectors.toList());

        pool.addAll(newChildren);

        // 2. Adult selection - The fight for a place! Survival
        List<T> newPopulation = performAdultSelection(pool, generation);

        // todo mutate for all, or only children
        return newPopulation;
    }

    private T getInstanceOf() {
        try {
            return reference.newInstance();
        } catch (InstantiationException | IllegalAccessException e ) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @return null if error
     */
    public Evolution build(){
        if(mutatation == null) {
            System.err.println("Build abort: Missing mutatation function");
            return null;
        }
        else if(crossover == null) {
            System.err.println("Build abort: Missing crossover function");
            return null;
        }

        System.out.println("==== Evolution algorithm ready ====");
        System.out.println("Mutator: " + mutatation);
        System.out.println("Crossover: " + crossover);
        System.out.println("Parent pool size: " + PARENT_POOL_SIZE);
        System.out.println("Children pool size: " + CHIlDREN_POOL_SIZE);
        System.out.println("Adult strategy: " + selectionsStrategy.name());
        System.out.println("Parent strategy: " + matingStrategy.name());
        System.out.println("Eliteism       : " + eliteism);


        return this;
    }


    // -------------------------------------
    // Getters and setters
    // -------------------------------------

    public Evolution<T> setAdultSelectionsStrategy(AdultSelection selectionsStrategy) {
        this.selectionsStrategy = selectionsStrategy;

        return this;
    }

    public Evolution<T> setMatingStrategy(ParentSelection matingStrategy) {
        this.matingStrategy = matingStrategy;
        return this;
    }

    public Evolution<T> setMutation(IMutation mutatation) {
        this.mutatation = mutatation;
        return this;
    }

    public Evolution<T> setCrossover(ICrossover crossover) {
        this.crossover = crossover;
        return this;
    }

    public Evolution<T> setPARENT_POOL_SIZE(int PARENT_POOL_SIZE) {
        this.PARENT_POOL_SIZE = PARENT_POOL_SIZE;
        return this;
    }

    public Evolution<T> setCHIlDREN_POOL_SIZE(int CHIlDREN_POOL_SIZE) {
        this.CHIlDREN_POOL_SIZE = CHIlDREN_POOL_SIZE;
        return this;

    }

    public void setEliteism(int eliteism) {
        this.eliteism = eliteism;
    }

    public int getEliteism() {
        return eliteism;
    }
}
