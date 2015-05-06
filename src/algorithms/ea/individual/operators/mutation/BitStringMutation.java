package algorithms.ea.individual.operators.mutation;

import algorithms.ea.individual.Genotype;

import java.util.Random;

public class BitStringMutation implements IMutation {
    private Random random;

    public BitStringMutation() {
        this.random = new Random();
    }

    @Override
    public Genotype mutate(Genotype genotypes) {
        Genotype newBitSet = genotypes.makeCopy();
        double chance = 1 / (double)newBitSet.getFixedLength();
        for (int i = 0; i < newBitSet.getFixedLength(); i++) {
            if(random.nextDouble() < chance) newBitSet.getBitSetData().flip(i);
        }

        return newBitSet;
    }
}
