package algorithms.ea.individual.operators;

import algorithms.ea.individual.Genotype;
import algorithms.ea.individual.operators.crossover.ICrossover;
import algorithms.ea.individual.operators.mutation.IMutation;

import java.util.List;

/**
 * Helper methods to perform mutation and crossover.
 */
public class GeneticOperator {
    public static Genotype mutate(IMutation mutator, Genotype genotypes) {
        return mutator.mutate(genotypes);
    }

    public static List<Genotype> crossover(ICrossover crossoverer, Genotype genotypesA, Genotype genotypesB) {
        return crossoverer.crossover(genotypesA, genotypesB);
    }
}
