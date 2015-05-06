package algorithms.eann;

import algorithms.ann.NeuralNet;
import algorithms.ea.individual.Genotype;
import algorithms.ea.individual.Individual;
import helpers.Conversion;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class IndividualBrain extends Individual<Double> {
    private NeuralNet brain;

    public IndividualBrain() {
        // ----------------------------------
        // Artificial Neural Network
        // Inputs: 3
        // Outputs: 3
        // Hidden Layers: 2
        // Neuron in each hidden layer: 6
        // ----------------------------------
        brain = new NeuralNet(3, 3, 2, 6);

        // Get brain data and make genotype of it
        List<Double> weights = brain.getWeights(); // getfrom number of weights
        
        byte[] genotypeData = new byte[Double.BYTES * weights.size()]; // get all the weights
        IntStream.range(0, weights.size())
                .forEach(i ->
                    System.arraycopy(
                            Conversion.toByteArray(weights.get(i)),
                            0,
                            genotypeData,
                            i * Double.BYTES,
                            Double.BYTES
                    )
                );


        this.genotypes = new Genotype(genotypeData, weights.size());
    }

    public void buildPhenotypes() {
        this.phenotypes = IntStream.range(0, this.genotypes.getFixedLength())
                .mapToObj(i -> {
                    byte[] buffer = new byte[Double.BYTES];
                    System.arraycopy(this.genotypes.getActualData(), i * Double.BYTES, buffer, 0, Double.BYTES);
                    return Conversion.toDouble(buffer);
                })
                .collect(Collectors.toList());
    }

    /**
     * Inserts the phenotypes to the brain as weights
     */
    public void rewireBrain(){
        brain.setWeights(this.getPhenotypes());
    }


    public List<Double> think(List<Double> input){
        return brain.update(input);
    }
}
