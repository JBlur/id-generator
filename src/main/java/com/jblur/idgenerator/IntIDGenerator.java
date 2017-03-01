package com.jblur.idgenerator;

import com.google.common.primitives.Ints;

public class IntIDGenerator extends IDGenerator {
    public IntIDGenerator(long epochStartTime, long uniqueID, int timeBitsCount, int uniqueIdBitsCount, int sequenceBitsCount, IDMode idMode) throws ZeroBitsExceptions, BitsCountException, NegativeBitsCountException {
        super(epochStartTime, uniqueID, timeBitsCount, uniqueIdBitsCount, sequenceBitsCount, idMode);
        if(getTotalBitsCount()!=32){
            throw new BitsCountException("Bits count isn't correct. Integer has to have exactly 32 bits");
        }
    }

    public int generateIntId() throws SequenceOverflowException {
        return Ints.fromByteArray(generateId());
    }
}
