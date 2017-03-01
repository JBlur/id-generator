package com.jblur.idgenerator;

import com.google.common.primitives.Shorts;

public class ShortIDGenerator extends IDGenerator {
    public ShortIDGenerator(long epochStartTime, long uniqueID, int timeBitsCount, int uniqueIdBitsCount, int sequenceBitsCount, IDMode idMode) throws ZeroBitsExceptions, BitsCountException, NegativeBitsCountException {
        super(epochStartTime, uniqueID, timeBitsCount, uniqueIdBitsCount, sequenceBitsCount, idMode);
        if(getTotalBitsCount()!=16){
            throw new BitsCountException("Bits count isn't correct. Short has to have exactly 16 bits");
        }
    }

    public short generateShortId() throws SequenceOverflowException {
        return Shorts.fromByteArray(generateId());
    }
}
