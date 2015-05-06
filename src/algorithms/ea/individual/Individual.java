package algorithms.ea.individual;

import algorithms.ea.individual.operators.GeneticOperator;
import algorithms.ea.individual.operators.mutation.IMutation;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collector;

public class Individual<T> implements Comparable<Individual>{

    // Unique id
    private static int idCounter = 0;
    private SimpleIntegerProperty id;

    protected List<T> phenotypes;
    protected Genotype genotypes;

    SimpleDoubleProperty fitness;
    //private double fitness;

    /**
     * Generation of the individual
     */
    private SimpleIntegerProperty age;


    public Individual(){
        age = new SimpleIntegerProperty(0);
        id = new SimpleIntegerProperty(idCounter++);
        fitness = new SimpleDoubleProperty(0);
    }

    /**
     * @param data
     */
    @Deprecated
    public Individual(Genotype data){
        this.genotypes = data;
    }

    public void mutate(IMutation mutator){
        this.genotypes = GeneticOperator.mutate(mutator, genotypes);
    }

    //public void convertToPhenotype(IPhenotypeBuilder builder){
    //    phenotypes = PhenotypeBuilder.translate(builder, genotypes);
    //}

    /**
     * Use setFitness instead
     * @param fitnessFunction
     */
//    @Deprecated
//    public void calculateFitness(IFitness fitnessFunction){
//        fitness = EvaluateFitness.Evaluate(fitnessFunction, phenotypes);
//    }

    private Collector<T, StringJoiner, String> phenotypeCollector =
            Collector.of(
                    () -> new StringJoiner(", "),           // supplier
                    (j, p) -> j.add(String.valueOf(p)),     // accumulator
                    StringJoiner::merge,                    // combiner (j1, j2) -> j1.merge(j2)
                    StringJoiner::toString                  // finisher
            );

    @Override
    public String toString() {
        return "Individual " + getId() +  " : ->Gen: " + getAge() + " Fitness: " + fitness
                + "\n\tGenotypes:  " + genotypes.toString()
                + "\n\tPhenotypes: " + phenotypes.stream().collect(phenotypeCollector);
    }


    public void setAge(int age) {
        this.age.set(age);
    }

    public void setGenotypes(Genotype genotypes) {
        this.genotypes = genotypes;
    }

    public Genotype getGenotypes() {
        return genotypes;
    }

    public double getFitness() {
        return fitness.get();
    }
    public void setFitness(double fitness) {
        this.fitness.set(fitness);
         //fitness = fitness;
    }

    public int getAge() {
        return age.get();
    }

    public int getId() {
        return id.get();
    }

    public List<T> getPhenotypes(){
        return phenotypes;
    }

    @Override
    public int compareTo(Individual o) {
        return Double.compare(getFitness(), o.getFitness());
    }

}
