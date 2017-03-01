package com.jblur.idgenerator;

import com.google.common.primitives.Longs;

public class LongIDGenerator extends IDGenerator{
    public LongIDGenerator(long epochStartTime, long uniqueID, int timeBitsCount, int uniqueIdBitsCount, int sequenceBitsCount, IDMode idMode) throws ZeroBitsExceptions, BitsCountException, NegativeBitsCountException {
        super(epochStartTime, uniqueID, timeBitsCount, uniqueIdBitsCount, sequenceBitsCount, idMode);
        if(getTotalBitsCount()!=64){
            throw new BitsCountException("Bits count isn't correct. Long has to have exactly 64 bits");
        }
    }

    public long generateLongId() throws SequenceOverflowException {
        return Longs.fromByteArray(generateId());
    }
}
