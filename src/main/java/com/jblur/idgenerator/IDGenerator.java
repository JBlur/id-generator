package com.jblur.idgenerator;

import org.apache.commons.lang3.mutable.MutableInt;

import java.math.BigInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Main unique ID generator for distributed environments.
 */
public class IDGenerator {
    private volatile long sequence=0;
    private Lock sequenceLock = new ReentrantLock();
    private volatile long lastTime=0;
    private final long epochStartTime, maxSequence, totalBitsCount;
    private final int bytesCount, trashBitsCount;
    private final long instanceID;
    private final int param1BitsCount, param2BitsCount, param3BitsCount;
    private final IDMode idMode;
    private byte trashBitsMask;

    /**
     * @param  epochStartTime  timestamp from which your project epoch is started. I.e. it must be the time your
     *                         project is publicly started. After that you have to always use the same epoch in each
     *                         instance.
     * @param  instanceID  unique ID of the IDGenerator instance. If you have only one instance in your project
     *                     then this parameter can be set to any number uniqueIdBitsCount parameter has to be set to 0
     * @param  timeBitsCount  count of time bits. More bits = more time your generated IDs will be guaranteed to be
     *                        unique.
     * @param  uniqueIdBitsCount  count of unique ID bits. More bits = more instances can run in parallel.
     * @param  sequenceBitsCount  count of sequence bits. More bits = more unique IDs can be generated per millisecond.
     *                            0 bits = 1 id per millisecond. 1 bits = 2 ids per millisecond. 2 bits = 4 ids per
     *                            millisecond. 8 bits = max 256 ids per millisecond.
     * @param  idMode  order mode in which bits of time, instance id and sequence are placed.
     * @see    IDMode
     */
    public IDGenerator(long epochStartTime, long instanceID, int timeBitsCount, int uniqueIdBitsCount,
                       int sequenceBitsCount, IDMode idMode) throws ZeroBitsExceptions, NegativeBitsCountException {
        this.totalBitsCount = timeBitsCount + uniqueIdBitsCount + sequenceBitsCount;
        if(this.totalBitsCount==0){
            throw new ZeroBitsExceptions("At least one bit have to be available to generate ID");
        }
        if(timeBitsCount<0 || uniqueIdBitsCount<0 || sequenceBitsCount<0){
            throw new NegativeBitsCountException("Bits count can not be less than 0");
        }
        this.epochStartTime = epochStartTime;
        this.instanceID = instanceID;

        switch (idMode){
            case TIME_UID_SEQUENCE: default:
                this.param1BitsCount = timeBitsCount;
                this.param2BitsCount = uniqueIdBitsCount;
                this.param3BitsCount = sequenceBitsCount;
                break;
            case TIME_SEQUENCE_UID:
                this.param1BitsCount = timeBitsCount;
                this.param2BitsCount = sequenceBitsCount;
                this.param3BitsCount = uniqueIdBitsCount;
                break;
            case UID_TIME_SEQUENCE:
                this.param1BitsCount = uniqueIdBitsCount;
                this.param2BitsCount = timeBitsCount;
                this.param3BitsCount = sequenceBitsCount;
                break;
            case UID_SEQUENCE_TIME:
                this.param1BitsCount = uniqueIdBitsCount;
                this.param2BitsCount = sequenceBitsCount;
                this.param3BitsCount = timeBitsCount;
                break;
            case SEQUENCE_TIME_UID:
                this.param1BitsCount = sequenceBitsCount;
                this.param2BitsCount = timeBitsCount;
                this.param3BitsCount = uniqueIdBitsCount;
                break;
            case SEQUENCE_UID_TIME:
                this.param1BitsCount = sequenceBitsCount;
                this.param2BitsCount = uniqueIdBitsCount;
                this.param3BitsCount = timeBitsCount;
                break;
        }
        this.maxSequence = new BigInteger("2").pow(sequenceBitsCount).longValue()-1;
        this.bytesCount = (int) Math.ceil(this.totalBitsCount/8.0);
        this.trashBitsCount = (int)(this.bytesCount*8 - this.totalBitsCount);
        this.idMode = idMode;

        if(this.trashBitsCount>0){
            this.trashBitsMask = (byte) (0b01111111 >> this.trashBitsCount-1);
        } else {
            this.trashBitsMask = (byte) 0b11111111;
        }
    }

    private IDModeParamsWrapper setupParams(long time, long sequence){
        IDModeParamsWrapper paramsWrapper;
        switch (this.idMode){
            case TIME_UID_SEQUENCE:
                paramsWrapper = new IDModeParamsWrapper(time, this.instanceID, sequence);
                break;
            case TIME_SEQUENCE_UID:
                paramsWrapper = new IDModeParamsWrapper(time, sequence, this.instanceID);
                break;
            case UID_TIME_SEQUENCE:
                paramsWrapper = new IDModeParamsWrapper(this.instanceID, time, sequence);
                break;
            case UID_SEQUENCE_TIME:
                paramsWrapper = new IDModeParamsWrapper(this.instanceID, sequence, time);
                break;
            case SEQUENCE_TIME_UID:
                paramsWrapper = new IDModeParamsWrapper(sequence, time, this.instanceID);
                break;
            case SEQUENCE_UID_TIME:
                paramsWrapper = new IDModeParamsWrapper(sequence, this.instanceID, time);
                break;
            default:
                paramsWrapper = null;
        }
        return paramsWrapper;
    }

    private byte generateByte(MutableInt partBitsLeft, MutableInt paramBitsLeft, long param){
        if((paramBitsLeft.intValue()-partBitsLeft.intValue())<0){
            partBitsLeft.subtract(paramBitsLeft);
            paramBitsLeft.setValue(0);
            return (byte) (param << partBitsLeft.intValue());
        }
        paramBitsLeft.subtract(partBitsLeft);
        partBitsLeft.setValue(0);
        return (byte) (param >> paramBitsLeft.intValue());
    }

    private byte generateByte(MutableInt partBitsLeft, MutableInt paramBitsLeft, long param, byte part){
        byte leftBitsMask = (byte) (0b11111111 >>> (8-partBitsLeft.intValue()));
        if((paramBitsLeft.intValue()-partBitsLeft.intValue())<0){
            partBitsLeft.subtract(paramBitsLeft);
            paramBitsLeft.setValue(0);
            return (byte) (part | (leftBitsMask & (param << partBitsLeft.intValue())));
        }
        paramBitsLeft.subtract(partBitsLeft);
        partBitsLeft.setValue(0);
        return (byte) (part | (leftBitsMask & (param >> paramBitsLeft.intValue())));
    }

    /**
     * Generates a byte array which consists of time bits, uid bits and sequence bits.<br>
     * Position of bits depends on IDMode.<br>
     * The first byte contains highest bits. Left bits are highest bits.<br>
     * For example:<br>
     * Number 285 (0b0000000100011101) will be presented in the byte array 'result' which will contain two bytes:<br>
     * result[0] will contain number: 1 (0b00000001)<br>
     * result[1] will contain number: 29 (0b00011101)<br>
     * <br>
     * UniqueID cannot contain more than 64 bits.
     * Time and sequence can contain more than 64 bits, but is doesn't make sense.<br>
     * <br>
     * Time and sequence can contain more than 64 bits, but is doesn't make sense as high bits of time and sequence
     * which are more than 64 bits will contain only 0s.<br>
     * The logical maximum amount of bits is 192 (64 bits of time, 64 bits of the unique ID, 64 bits of sequence).
     * You can use more bits for time and sequence if you definitely know that you need so.<br>
     * <br>
     * Common use case is to use 64 bits which consist of 41 bits of time and other bits are depend on your
     * system.<br>
     * For example:<br>
     * Twitter Snowflake: 41 bits of time, 11 bits of machine ID, 12 bits of sequence number.
     * ({@link IDMode#TIME_UID_SEQUENCE})<br>
     * Instagram: 41 bits of time, 13 bits of machine ID, 10 bits of sequence number.
     * ({@link IDMode#TIME_UID_SEQUENCE})
     *
     * @return      Returns generated unique ID where the first byte contains highest bits and left bits in each byte
     * are highest bits.
     *
     */
    public byte[] generateId() throws SequenceOverflowException {
        long floorTime = System.currentTimeMillis() - this.epochStartTime;
        long sequence;

        this.sequenceLock.lock();
        try{
            if(floorTime<=this.lastTime){
                sequence = ++this.sequence;
                if(sequence>this.maxSequence){
                    throw new SequenceOverflowException();
                }
                floorTime = this.lastTime;
            } else {
                sequence = 0L;
                this.sequence = 0L;
                this.lastTime=floorTime;
            }
        }finally {
            this.sequenceLock.unlock();
        }

        IDModeParamsWrapper paramsWrapper = setupParams(floorTime, sequence);
        byte[] result = new byte[this.bytesCount];
        MutableInt param1BitsLeft = new MutableInt(this.param1BitsCount),
                param2BitsLeft = new MutableInt(this.param2BitsCount),
                param3BitsLeft = new MutableInt(this.param3BitsCount),
                bitsLeft = new MutableInt(8-this.trashBitsCount);

        for(int i=0;i<result.length;i++){
            if(i>0) bitsLeft.setValue(8);
            if(param1BitsLeft.intValue()>0){
                result[i] = generateByte(bitsLeft, param1BitsLeft, paramsWrapper.param1);
                if(bitsLeft.intValue()==0) continue;
            }
            if(param2BitsLeft.intValue()>0){
                if(bitsLeft.intValue()==8)
                    result[i] = generateByte(bitsLeft, param2BitsLeft, paramsWrapper.param2);
                else
                    result[i] = generateByte(bitsLeft, param2BitsLeft, paramsWrapper.param2, result[i]);
                if(bitsLeft.intValue()==0) continue;
            }
            if(param3BitsLeft.intValue()>0){
                if(bitsLeft.intValue()==8)
                    result[i] = generateByte(bitsLeft, param3BitsLeft, paramsWrapper.param3);
                else
                    result[i] = generateByte(bitsLeft, param3BitsLeft, paramsWrapper.param3, result[i]);
            }
        }

        if(this.trashBitsCount>0){
            result[0] &= this.trashBitsMask;
        }

        return result;
    }

    /**
     * @return      Current sequence number
     */
    public long getSequence() {
        return this.sequence;
    }

    /**
     * @return      Last time id was generated
     */
    public long getLastTime() {
        return this.lastTime;
    }

    /**
     * @return      UTC time from which your epoch of the project is started
     */
    public long getEpochStartTime() {
        return this.epochStartTime;
    }

    /**
     * @return      Unique instance ID
     */
    public long getInstanceID() {
        return this.instanceID;
    }

    /**
     * @return      Bits count of the first param
     */
    public int getParam1BitsCount() {
        return this.param1BitsCount;
    }

    /**
     * @return      Bits count of the second param
     */
    public int getParam2BitsCount() {
        return this.param2BitsCount;
    }

    /**
     * @return      Bits count of the third param
     */
    public int getParam3BitsCount() {
        return this.param3BitsCount;
    }

    /**
     * @return      Maximum generated ids count per millisecond
     */
    public long getMaxSequence() {
        return this.maxSequence;
    }

    /**
     * @return      Total useful bits count
     */
    public long getTotalBitsCount() {
        return this.totalBitsCount;
    }

    /**
     * @return      Total useless bits count
     */
    public int getTrashBitsCount() {
        return this.trashBitsCount;
    }

    /**
     * @return      Total bytes which will be returned each time id is generated
     */
    public int getBytesCount() {
        return this.bytesCount;
    }

    /**
     * @return      ID mode
     */
    public IDMode getIdMode() {
        return idMode;
    }
}