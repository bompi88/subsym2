package algorithms.ea;

import algorithms.ea.individual.Individual;

public class IndiviualWrapper<T extends Individual> implements Comparable<IndiviualWrapper>{
    public T individual;
    public double fitness;


    public IndiviualWrapper(T individual, double fitness) {
        this.individual = individual;
        this.fitness = fitness;
    }

    public double getFitness() {
        return fitness;
    }

    @Override
    public int compareTo(IndiviualWrapper o) {
        if(getFitness() == o.getFitness()) return 0;
        else if(getFitness() > o.getFitness()) return 1;
        return 0;
    }
}
