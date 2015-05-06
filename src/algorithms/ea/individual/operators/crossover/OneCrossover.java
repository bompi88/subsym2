package algorithms.ea.individual.operators.crossover;

import algorithms.ea.Constants;
import algorithms.ea.individual.Genotype;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class OneCrossover implements ICrossover {
    Random random = new Random();

    @Override
    public List<Genotype> crossover(Genotype genotypesA, Genotype genotypesB) {
        int length = genotypesA.getActualData().length;
        int crossoverPoint = random.nextInt(length);

        List<Genotype> genotypes = new LinkedList<>();
        if(random.nextDouble() < Constants.CROSSOVER_CHANCE) {
            Genotype n1 = new Genotype(length, genotypesA.getFixedLength());

            System.arraycopy(genotypesB.getActualData(), 0, n1.getActualData(), 0, crossoverPoint);
            System.arraycopy(genotypesA.getActualData(), crossoverPoint, n1.getActualData(), crossoverPoint, length - crossoverPoint);

            genotypes.add(n1);
        }
        if(random.nextDouble() < Constants.CROSSOVER_CHANCE) {
            Genotype n2 = new Genotype(length, genotypesA.getFixedLength());

            System.arraycopy(genotypesA.getActualData(), 0, n2.getActualData(), 0, crossoverPoint);
            System.arraycopy(genotypesB.getActualData(), crossoverPoint, n2.getActualData(), crossoverPoint, length - crossoverPoint);

            genotypes.add(n2);

        }

        return genotypes;
    }
}
