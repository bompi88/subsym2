package algorithms.ea.individual;


import helpers.Conversion;

import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * GenoType is one property
 * todo BitSet, then this class can be removed..
 */
public class Genotype {

    private byte[] actualData;

    private int length = 0;


    /**
     * Setup a new Genotype. Copies the genotype data.
     * @param genotype
     * @param length
     */
    public Genotype(byte[] genotype, int length) {
        this.length = length;

        this.actualData = new byte[genotype.length];

        System.arraycopy(genotype, 0, this.actualData, 0, genotype.length);
    }

    public Genotype(int datalength, int length) {
        this.actualData = new byte[datalength];
        this.length = length;
    }

    public byte[] getActualData(){
        return this.actualData;
    }

    public Genotype makeCopy() {
        return new Genotype(actualData, length);
    }


    @Deprecated
    public BitSet getBitSetData() {
        return Conversion.fromByteArray(actualData);
    }


    public int getFixedLength() {
        return length;
    }

    @Override
    public String toString(){

        String print = "";

        BitSet data = Conversion.fromByteArray(actualData);

        for(int i = 0; i < getFixedLength(); i++) {
            print += data.get(i) ? "1" : "0";
        }
        return print;
    }
}
