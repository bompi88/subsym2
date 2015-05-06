package algorithms.ectrnn;

import algorithms.ea.individual.Genotype;
import algorithms.ea.individual.Individual;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Each Neuron type has a different lower-upper bound. So
 * they need to get the Byte
 */
public class IndividualCTRBrain extends Individual<Byte>{

    private CTRNeuralNet brain;

    public static int inputLayers = 5;
    public static int outputLayers = 2;


    int noWeightsUsedInBrain;

    public IndividualCTRBrain() {
        // ----------------------------------
        // Artificial Neural Network
        // Inputs: 5 or 7
        // Outputs: 2
        // Hidden Layers: 1
        // Neuron in each hidden layer: 2
        // ----------------------------------
        brain = new CTRNeuralNet(inputLayers, outputLayers, 1, 2);

        // Get brain data and make genotype of it
        // todo getWeights must return correct Byte
        List<Byte> weights = brain.getWeights(); // getfrom number of weights

        noWeightsUsedInBrain = weights.size();

        byte[] genotypeData = new byte[weights.size()]; // get all the weights
        IntStream.range(0, weights.size())
                .forEach(i -> genotypeData[i] = weights.get(i));

        this.genotypes = new Genotype(genotypeData, weights.size());
    }


    public void buildPhenotypes() {
        this.phenotypes = IntStream.range(0, this.genotypes.getActualData().length)
                .mapToObj(i -> this.genotypes.getActualData()[i])
                .collect(Collectors.toList());
        if(this.phenotypes.size() != noWeightsUsedInBrain) {
            System.err.println("Fatal phenotype error: Wrong amount. " + this.phenotypes.size() + " " + noWeightsUsedInBrain);
        }
    }


    /**
     * Inserts the phenotypes to the brain as weights
     */
    public void rewireBrain(){
        brain.setWeights(this.getPhenotypes());
    }

    public List<Double> think(List<Double> input){
        //System.out.println(brain);
        return brain.update(input);
    }
}
