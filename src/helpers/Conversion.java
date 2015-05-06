package helpers;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class Conversion {
    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static Double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    // Returns a bitset containing the values in bytes.
    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    private static int bitsNeededInLong(long value) {
        int index = 0;
        int no = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                no = index;
            }
            ++index;
            value = value >>> 1;
        }
        return no + 1;
    }

    public static BitSet convertLongToBitSet(long value) {
        BitSet bits = new BitSet();
        int index = 0;
        while (value != 0L) {
            if (value % 2L != 0) {
                bits.set(index);
            }
            ++index;
            value = value >>> 1;
        }
        return bits;
    }

}
