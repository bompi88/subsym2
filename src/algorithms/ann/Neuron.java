package algorithms.ann;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class Neuron {

    // Weights 1..N, N+1 is activation value
    private List<Double> weights;

    int numberOfInputs;

    public Neuron(int inputs){
        Random random = new Random();
        numberOfInputs = inputs + 1;

        weights = new LinkedList<>();

        IntStream.range(0, numberOfInputs + 1) // todo +1 er nok en bug?
                .forEach(i -> weights.add(random.nextDouble()));
    }

    public int getNumberOfWeights(){
        return numberOfInputs;
    }

    public Double getWeight(int index){
        return weights.get(index);
    }
    public Double setWeight(int index, double value){
        return weights.set(index, value);
    }
}
