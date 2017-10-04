package com.jblur.idgenerator;

/**
 * Unique byte ID generator for distributed environments.
 */
public class ByteIDGenerator extends IDGenerator {
    public ByteIDGenerator(long epochStartTime, long uniqueID, int timeBitsCount, int uniqueIdBitsCount, int sequenceBitsCount, IDMode idMode) throws ZeroBitsExceptions, BitsCountException, NegativeBitsCountException {
        super(epochStartTime, uniqueID, timeBitsCount, uniqueIdBitsCount, sequenceBitsCount, idMode);
        if(getTotalBitsCount()!=8){
            throw new BitsCountException("Bits count isn't correct. Byte has to have exactly 8 bits");
        }
    }

    public byte generateByteId() throws SequenceOverflowException {
        return generateId()[0];
    }
}
