package algorithms.ectrnn;


import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Continuous time recurrent neural net
 *
 * todo neuroner har state, m� kunne resettes
 */
public class CTRNeuralNet {
    private int numInputs;
    private int numOutputs;
    private int numHiddenLayers;
    private int neuronsPerHiddenLayer;

    // numHiddenLayers + outputLayer
    private List<NeuronLayer> neuronLayers;

    public CTRNeuralNet(int numInputs, int numOutputs, int numHiddenLayers, int neuronsPerHiddenLayer){
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.numHiddenLayers = numHiddenLayers;
        this.neuronsPerHiddenLayer = neuronsPerHiddenLayer;

        createNet();
    }

    private void createNet() {
        neuronLayers = new LinkedList<>();

        if(numHiddenLayers > 0) {
            // Add first hidden layer connected from input layer
            neuronLayers.add(new NeuronLayer(neuronsPerHiddenLayer, numInputs));

            // Add other layers
            for (int i = 0; i < numHiddenLayers - 1; i++) {
                neuronLayers.add(new NeuronLayer(neuronsPerHiddenLayer, neuronsPerHiddenLayer));
            }

            // Create output layer
            neuronLayers.add(new NeuronLayer(numOutputs, neuronsPerHiddenLayer));
        } else {
            // Create output layer
            neuronLayers.add(new NeuronLayer(numOutputs, numInputs));
        }
    }


    public List<Byte> getWeights() {
        //this will hold the weights
        List<Byte> weights = new LinkedList<>();

        //each layer
        for (int i=0; i < numHiddenLayers + 1; ++i){

            NeuronLayer nl = neuronLayers.get(i);
            //each neuron
            for (int j = 0; j < nl.neurons.size(); ++j) {

                Neuron neuron = nl.getNeurons().get(j);
                //for each weight
                for (int k=0; k < neuron.numberOfEvolvableWeights; ++k) {
                    weights.add(neuron.getByteWeight(k));
                }
            }
        }

        return weights;
    }

    public void setWeights(List<Byte> weights) {

        int cWeight = 0;

        //each layer
        for (int i=0; i < numHiddenLayers + 1; ++i) {
            NeuronLayer nl = neuronLayers.get(i);
            //each neuron
            for (int j = 0; j < nl.neurons.size(); ++j) {
                Neuron neuron = nl.getNeurons().get(j);
                //for each weight
                for (int k = 0; k < neuron.numberOfEvolvableWeights; ++k) {
                    // todo removed weights.add( will create bug?

                    neuron.setWeight(k, weights.get(cWeight++));
                }
            }
        }
    }

    /**
     * Basicly execute
     * @param inputs
     * @return
     */
    public List<Double> update(List<Double> inputs) {
        // The results
        List<Double> outputs = new LinkedList<>();



        // Incorrect amount if inputs. Return empty
        if(inputs.size() != numInputs) {
            // todo should throw exception
            System.err.println("Error wrong number of inputs: " + inputs.size() + ". Needed: " + numInputs);
            //throw new RuntimeException();

            return new LinkedList<>();
        }

        // walk through each layer. + 1 is output layer
        for (int i = 0; i < numHiddenLayers + 1; i++) {
            // When input layer has been traversed we update to the next layer
            if(i > 0)
                inputs = outputs;

            outputs = new LinkedList<>();//.clear();

            int cWeight = 0;

            // ================================================
            // Traverse neurons
            // ================================================
            for (int j = 0; j < neuronLayers.get(i).getNeurons().size(); j++) {

                double s = 0;

                Neuron cNeuron = neuronLayers.get(i).getNeurons().get(j);

                // Formula 1: Si = sum Oj * Wij   todo extra minus one to remove bias
                for (int k = 0; k < cNeuron.numberOfInputs -1 -1; k++) {
                    s += cNeuron.getWeight(k) * inputs.get(cWeight++);
                }

                // Get other lastOutputs on same layer (also takes self..
                for (int k = 0; k < neuronLayers.get(i).getNeurons().size(); k++) {
                    s += neuronLayers.get(i).getNeurons().get(k).getLastOutput() * cNeuron.getOtherLayerWeight(k);
                }

                // Formula 2
                double timeDerivative = (1 / cNeuron.getTime()) * (-1 * cNeuron.getY() + s + 1 * cNeuron.getBias());

                // Formula 3
                double output = 1 / (1 + Math.exp(-1 * cNeuron.getGain() * cNeuron.getY()));

                // todo before or after F3?
                cNeuron.addToY(timeDerivative);

                outputs.add(output);

                cWeight = 0;
            }

            // Update outputs..
            for (int j = 0; j < neuronLayers.get(i).getNeurons().size(); j++) {
                neuronLayers.get(i).getNeurons().get(j).setLastOutput(outputs.get(j));
            }
        }

        return outputs;
    }

    private double layerweight(final NeuronLayer neuronLayer, final List<Double> inputs) {
        return IntStream.range(0, neuronLayer.neurons.size())
                .mapToDouble(y ->
                                IntStream.range(0, neuronLayer.neurons.get(y).getNumberOfWeights())
                                        .mapToDouble(x -> neuronLayer.neurons.get(y).getWeight(x) * inputs.get(x))
                                        .sum()
                )
                .sum();
    }


    private double activationFunction(Double netInput, double p) {
        return 1 / (1 + Math.exp( -netInput/p));
    }

    public String toString() {
        String str = "{numberOfInputs: " + numInputs +  ", numOfOutputs: " + numOutputs + ", numHiddenLayers: " + numHiddenLayers + ", numOfNeuronsHiddenLayer: " + neuronsPerHiddenLayer + " }\n";

        for(NeuronLayer layer : neuronLayers) {
            str += layer.toString() + "\n";
        }

        return str + "\n\n";
    }

}
