package com.jblur.idgenerator;

import java.util.BitSet;

/**
 * Unique BitSet ID generator for distributed environments.
 */
public class BitSetIDGenerator extends IDGenerator{
    public BitSetIDGenerator(long epochStartTime, long uniqueID, int timeBitsCount, int uniqueIdBitsCount, int sequenceBitsCount, IDMode idMode) throws ZeroBitsExceptions, NegativeBitsCountException {
        super(epochStartTime, uniqueID, timeBitsCount, uniqueIdBitsCount, sequenceBitsCount, idMode);
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

    public BitSet generateBitSetId() throws SequenceOverflowException {
        return bitSetFromByteArray(generateId(), getTrashBitsCount());
    }
}
