package algorithms.ea.individual.operators.crossover;

import algorithms.ea.individual.Genotype;

import java.util.List;

public interface ICrossover {

    List<Genotype> crossover(Genotype genotypesA, Genotype genotypesB);
}
