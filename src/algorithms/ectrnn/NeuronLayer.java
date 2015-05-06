package algorithms.ectrnn;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class NeuronLayer {

    List<Neuron> neurons;

    public NeuronLayer(int numNeurons, int inputsPerNeuron){
        neurons = new LinkedList<>();
        IntStream.range(0, numNeurons)
                .forEach(i -> neurons.add(new Neuron(inputsPerNeuron, numNeurons)));
    }


    public List<Neuron> getNeurons(){
        return neurons;
    }

    public String toString() {
        return neurons.toString();
    }
}
