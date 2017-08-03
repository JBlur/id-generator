import com.google.common.primitives.Ints;
import com.jblur.idgenerator.*;
import javafx.util.Pair;
import org.junit.Test;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IntIDGeneratorTest {
    private BitSet bitSetFromString(String bits){
        BitSet bitSet = new BitSet(bits.length());
        for(int i=0; i<bits.length();i++){
            bitSet.set(i, bits.charAt(i)=='1');
        }
        return bitSet;
    }

    private long longFromBitsString(String bits){
        BitSet bitSet = new BitSet(64);
        for(int i=0, j=bits.length()-1; j>=0;i++,j--){
            bitSet.set(i, bits.charAt(j)=='1');
        }
        long value = 0L;
        for (int i = 0; i < bitSet.length(); ++i) {
            value += bitSet.get(i) ? (1L << i) : 0L;
        }
        return value;
    }

    private static BitSet bitSetFromByteArray(byte[] bytes, int trashBits){
        if(trashBits>8) trashBits=8;
        int bitsCount = bytes.length*8-trashBits;
        BitSet bits = new BitSet(bitsCount);
        for(int i=0;i<bitsCount;i++){
            bits.set(i, ((bytes[(i+trashBits)/8]>>(7 - (i+trashBits)%8)) &1)==1);
        }
        return bits;
    }

    private void testUid(String uidBits, List<Pair<Integer, Integer>> timeAndSequenceBitsCountList) throws Exception{
        BitSet uidBitSet = bitSetFromString(uidBits);
        long uid = longFromBitsString(uidBits);
        IntIDGenerator idGenerator;
        for(Pair<Integer, Integer> timeAndSequenceBitsCount:timeAndSequenceBitsCountList){
            idGenerator = new IntIDGenerator(0,
                    uid,
                    timeAndSequenceBitsCount.getKey(), //time bits count
                    uidBits.length(),
                    timeAndSequenceBitsCount.getValue(), //sequence bits count
                    IDMode.TIME_UID_SEQUENCE);

            BitSet resultBitSet = bitSetFromByteArray(Ints.toByteArray(idGenerator.generateIntId()), idGenerator.getTrashBitsCount());
            for(int i=0;i<uidBits.length();i++){
                assertThat(uidBitSet.get(i), is(equalTo(resultBitSet.get(i+timeAndSequenceBitsCount.getKey()))));
            }

            idGenerator = new IntIDGenerator(0,
                    uid,
                    timeAndSequenceBitsCount.getKey(), //time bits count
                    uidBits.length(),
                    timeAndSequenceBitsCount.getValue(), //sequence bits count
                    IDMode.TIME_SEQUENCE_UID);
            resultBitSet = bitSetFromByteArray(Ints.toByteArray(idGenerator.generateIntId()), idGenerator.getTrashBitsCount());
            for(int i=0;i<uidBits.length();i++){
                assertThat(uidBitSet.get(i), equalTo(resultBitSet.get(i+timeAndSequenceBitsCount.getKey()+timeAndSequenceBitsCount.getValue())));
            }

            idGenerator = new IntIDGenerator(0,
                    uid,
                    timeAndSequenceBitsCount.getKey(), //time bits count
                    uidBits.length(),
                    timeAndSequenceBitsCount.getValue(), //sequence bits count
                    IDMode.SEQUENCE_TIME_UID);
            resultBitSet = bitSetFromByteArray(Ints.toByteArray(idGenerator.generateIntId()), idGenerator.getTrashBitsCount());
            for(int i=0;i<uidBits.length();i++){
                assertThat(uidBitSet.get(i), equalTo(resultBitSet.get(i+timeAndSequenceBitsCount.getKey()+timeAndSequenceBitsCount.getValue())));
            }

            idGenerator = new IntIDGenerator(0,
                    uid,
                    timeAndSequenceBitsCount.getKey(), //time bits count
                    uidBits.length(),
                    timeAndSequenceBitsCount.getValue(), //sequence bits count
                    IDMode.SEQUENCE_UID_TIME);
            resultBitSet = bitSetFromByteArray(Ints.toByteArray(idGenerator.generateIntId()), idGenerator.getTrashBitsCount());
            for(int i=0;i<uidBits.length();i++){
                assertThat(uidBitSet.get(i), equalTo(resultBitSet.get(i+timeAndSequenceBitsCount.getValue())));
            }

            idGenerator = new IntIDGenerator(0,
                    uid,
                    timeAndSequenceBitsCount.getKey(), //time bits count
                    uidBits.length(),
                    timeAndSequenceBitsCount.getValue(), //sequence bits count
                    IDMode.UID_SEQUENCE_TIME);
            resultBitSet = bitSetFromByteArray(Ints.toByteArray(idGenerator.generateIntId()), idGenerator.getTrashBitsCount());
            for(int i=0;i<uidBits.length();i++){
                assertThat(uidBitSet.get(i), equalTo(resultBitSet.get(i)));
            }

            idGenerator = new IntIDGenerator(0,
                    uid,
                    timeAndSequenceBitsCount.getKey(), //time bits count
                    uidBits.length(),
                    timeAndSequenceBitsCount.getValue(), //sequence bits count
                    IDMode.UID_TIME_SEQUENCE);
            resultBitSet = bitSetFromByteArray(Ints.toByteArray(idGenerator.generateIntId()), idGenerator.getTrashBitsCount());
            for(int i=0;i<uidBits.length();i++){
                assertThat(uidBitSet.get(i), equalTo(resultBitSet.get(i)));
            }
        }
    }

    @Test
    public void testUIDPosition() throws Exception {
        List ordinaryList = new LinkedList<Pair<Integer, Integer>>() {{
            add(new Pair<Integer, Integer>(16, 5));
        }};

        testUid("00000000001", ordinaryList);
        testUid("01001000011", ordinaryList);
        testUid("00000000010", ordinaryList);
        testUid("00000000000", ordinaryList);
        testUid("11111111111", ordinaryList);
        testUid("",
                new LinkedList<Pair<Integer, Integer>>() {{
                    add(new Pair<Integer, Integer>(28, 4));
                    add(new Pair<Integer, Integer>(32, 0));
                    add(new Pair<Integer, Integer>(0, 32));
                    add(new Pair<Integer, Integer>(4, 28));
                }}
        );
        testUid("01010111111111111100000001010101",
                new LinkedList<Pair<Integer, Integer>>() {{add(new Pair<Integer, Integer>(0, 0));}});
    }

    @Test(expected = NegativeBitsCountException.class)
    public void testNegativeBitsCount() throws Exception {
        testUid("01010111111111111100000001010101",
                new LinkedList<Pair<Integer, Integer>>() {{add(new Pair<Integer, Integer>(-1, 1));}});
    }

    @Test(expected = ZeroBitsExceptions.class)
    public void testZeroBitsCount() throws Exception {
        testUid("", new LinkedList<Pair<Integer, Integer>>() {{add(new Pair<Integer, Integer>(0, 0));}});
    }

    @Test(expected = BitsCountException.class)
    public void testBitsCountOverflow() throws Exception {
        testUid("1010101", new LinkedList<Pair<Integer, Integer>>() {{add(new Pair<Integer, Integer>(20, 10));}});
    }
}
