package algorithms.ea.individual.operators.mutation;

import algorithms.ea.individual.Genotype;

import java.util.Random;

public class MaybeMutator implements IMutation {
    double chance;
    Random random;
    public MaybeMutator(double chance) {
        this.chance = chance;
        this.random = new Random();
    }

    @Override
    public Genotype mutate(Genotype genotypes) {
        Genotype newBitSet = genotypes.makeCopy();

        if(random.nextDouble() < chance) {
            byte[] next = new byte[1];
            random.nextBytes(next);
            int randomPos = random.nextInt(genotypes.getActualData().length);
            System.arraycopy(next, 0, newBitSet.getActualData(), randomPos, 1);
        }

        return newBitSet;
    }
}
