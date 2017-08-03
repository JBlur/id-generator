package com.jblur.idgenerator;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import java.math.BigInteger;

public class IDGenerator {
    private volatile MutableLong sequence=new MutableLong(0);
    private volatile long lastTime=0;
    private final long epochStartTime, maxSequence, totalBitsCount;
    private final int bytesCount, trashBitsCount;
    private final MutableLong uniqueID;
    private final MutableInt param1BitsCount, param2BitsCount, param3BitsCount;
    private final IDMode idMode;

    public IDGenerator(long epochStartTime, long uniqueID, int timeBitsCount, int uniqueIdBitsCount, int sequenceBitsCount, IDMode idMode) throws ZeroBitsExceptions, NegativeBitsCountException {
        this.totalBitsCount= timeBitsCount + uniqueIdBitsCount + sequenceBitsCount;
        if(this.totalBitsCount==0){
            throw new ZeroBitsExceptions("At least one bit have to be available to generate ID");
        }
        if(timeBitsCount<0 || uniqueIdBitsCount<0 || sequenceBitsCount<0){
            throw new NegativeBitsCountException("Bits count can not be less than 0");
        }
        this.epochStartTime=epochStartTime;
        this.uniqueID=new MutableLong(uniqueID);

        switch (idMode){
            case TIME_UID_SEQUENCE: default:
                this.param1BitsCount =new MutableInt(timeBitsCount);
                this.param2BitsCount =new MutableInt(uniqueIdBitsCount);
                this.param3BitsCount =new MutableInt(sequenceBitsCount);
                break;
            case TIME_SEQUENCE_UID:
                this.param1BitsCount =new MutableInt(timeBitsCount);
                this.param2BitsCount =new MutableInt(sequenceBitsCount);
                this.param3BitsCount =new MutableInt(uniqueIdBitsCount);
                break;
            case UID_TIME_SEQUENCE:
                this.param1BitsCount =new MutableInt(uniqueIdBitsCount);
                this.param2BitsCount =new MutableInt(timeBitsCount);
                this.param3BitsCount =new MutableInt(sequenceBitsCount);
                break;
            case UID_SEQUENCE_TIME:
                this.param1BitsCount =new MutableInt(uniqueIdBitsCount);
                this.param2BitsCount =new MutableInt(sequenceBitsCount);
                this.param3BitsCount =new MutableInt(timeBitsCount);
                break;
            case SEQUENCE_TIME_UID:
                this.param1BitsCount =new MutableInt(sequenceBitsCount);
                this.param2BitsCount =new MutableInt(timeBitsCount);
                this.param3BitsCount =new MutableInt(uniqueIdBitsCount);
                break;
            case SEQUENCE_UID_TIME:
                this.param1BitsCount =new MutableInt(sequenceBitsCount);
                this.param2BitsCount =new MutableInt(uniqueIdBitsCount);
                this.param3BitsCount =new MutableInt(timeBitsCount);
                break;
        }
        this.maxSequence=new BigInteger("2").pow(sequenceBitsCount).longValue()-1;
        this.bytesCount=(int) Math.ceil(this.totalBitsCount/8.0);
        this.trashBitsCount=(int)(bytesCount*8 - this.totalBitsCount);
        this.idMode=idMode;
    }

    private void setupParams(MutableLong param1, MutableLong param2, MutableLong param3, MutableLong time, Long sequence){
        switch (idMode){
            case TIME_UID_SEQUENCE:
                param1.setValue(time);
                param2.setValue(uniqueID);
                param3.setValue(sequence);
                break;
            case TIME_SEQUENCE_UID:
                param1.setValue(time);
                param2.setValue(sequence);
                param3.setValue(uniqueID);
                break;
            case UID_TIME_SEQUENCE:
                param1.setValue(uniqueID);
                param2.setValue(time);
                param3.setValue(sequence);
                break;
            case UID_SEQUENCE_TIME:
                param1.setValue(uniqueID);
                param2.setValue(sequence);
                param3.setValue(time);
                break;
            case SEQUENCE_TIME_UID:
                param1.setValue(sequence);
                param2.setValue(time);
                param3.setValue(uniqueID);
                break;
            case SEQUENCE_UID_TIME:
                param1.setValue(sequence);
                param2.setValue(uniqueID);
                param3.setValue(time);
                break;
        }
    }

    private byte generateByte(MutableInt partBitsLeft, MutableInt paramBitsLeft, MutableLong param){
        if((paramBitsLeft.intValue()-partBitsLeft.intValue())<0){
            partBitsLeft.subtract(paramBitsLeft);
            paramBitsLeft.setValue(0);
            return (byte) (param.longValue() << partBitsLeft.intValue());
        }
        paramBitsLeft.subtract(partBitsLeft);
        partBitsLeft.setValue(0);
        return (byte) (param.longValue() >> paramBitsLeft.intValue());
    }

    private byte generateByte(MutableInt partBitsLeft, MutableInt paramBitsLeft, MutableLong param, byte part){
        byte leftBitsMask = (byte) (0b11111111 >>> (8-partBitsLeft.intValue()));
        if((paramBitsLeft.intValue()-partBitsLeft.intValue())<0){
            partBitsLeft.subtract(paramBitsLeft);
            paramBitsLeft.setValue(0);
            return (byte) (part | (leftBitsMask & (param.longValue() << partBitsLeft.intValue())));
        }
        paramBitsLeft.subtract(partBitsLeft);
        partBitsLeft.setValue(0);
        return (byte) (part | (leftBitsMask & (param.longValue() >> paramBitsLeft.intValue())));
    }


    /*
     * Generates byte array which consists of time bits, uid bits and sequence bits.
     * Bits positions depends on IDMode.
     * First byte contains highest bits. Left bits are highest bits.
     * For example:
     * Number 285 (0b0000000100011101) will be presented in byte array 'result' which will contain two bytes:
     * result[0] will contain number: 1 (0b00000001)
     * result[1] will contain number: 29 (0b00011101)
     *
     * UniqueID can not contain more than 64 bits.
     * Time and sequence can contain more than 64 bits but is doesn't make sense.
     *
     * Time and sequence can contain more than 64 bits but is doesn't make sense as high bits of time and  sequence which are more than 64 bits will contain only 0s.
     * Logical maximum amount of bits is 192 (64 bits of time, 64 bits of unique ID, 64 bits of sequence). You can use more bits for time and sequence if you definitely know that you need so.
     *
     * Common use case is to use 64 bits which consists of 41 bits for time and other bits are depend of your system. For example:
     * Twitter Snowflake: 41 bits time, 11 bits machine ID, 12 bits sequence number. (IDMode.TIME_UID_SEQUENCE)
     * Instagram: 41 bits time, 13 bits machine ID, 10 bits sequence number. (IDMode.TIME_UID_SEQUENCE)
     *
     */
    public byte[] generateId() throws SequenceOverflowException{
        MutableLong param1 = new MutableLong(), param2 = new MutableLong(), param3 = new MutableLong();
        MutableLong floorTime = new MutableLong(System.currentTimeMillis() - this.epochStartTime);
        Long sequence = 0L;

        if(floorTime.longValue()==lastTime){
            sequence = this.sequence.incrementAndGet();
            if(sequence>this.maxSequence){
                throw new SequenceOverflowException();
            }
        }else{
            this.sequence.setValue(0);
        }
        lastTime=floorTime.longValue();
        setupParams(param1, param2, param3, floorTime, sequence);
        byte[] result = new byte[bytesCount];
        MutableInt param1BitsLeft = new MutableInt(param1BitsCount),
                param2BitsLeft = new MutableInt(param2BitsCount),
                param3BitsLeft = new MutableInt(param3BitsCount),
                bitsLeft = new MutableInt(8-trashBitsCount);

        for(int i=0;i<bytesCount;i++){
            if(i>0) bitsLeft.setValue(8);
            if(param1BitsLeft.intValue()>0){
                result[i] = generateByte(bitsLeft, param1BitsLeft, param1);
                if(bitsLeft.intValue()==0) continue;
            }
            if(param2BitsLeft.intValue()>0){
                if(bitsLeft.intValue()==8)
                    result[i] = generateByte(bitsLeft, param2BitsLeft, param2);
                else
                    result[i] = generateByte(bitsLeft, param2BitsLeft, param2, result[i]);
                if(bitsLeft.intValue()==0) continue;
            }
            if(param3BitsLeft.intValue()>0){
                if(bitsLeft.intValue()==8)
                    result[i] = generateByte(bitsLeft, param3BitsLeft, param3);
                else
                    result[i] = generateByte(bitsLeft, param3BitsLeft, param3, result[i]);
            }
        }

        if(trashBitsCount>0){
            result[0] &= (byte) (0b01111111 >> trashBitsCount-1);
        }

        return result;
    }

    public MutableLong getSequence() {
        return sequence;
    }

    public long getLastTime() {
        return lastTime;
    }

    public long getEpochStartTime() {
        return epochStartTime;
    }

    public MutableLong getUniqueID() {
        return uniqueID;
    }

    public MutableInt getParam1BitsCount() {
        return param1BitsCount;
    }

    public MutableInt getParam2BitsCount() {
        return param2BitsCount;
    }

    public MutableInt getParam3BitsCount() {
        return param3BitsCount;
    }

    public long getMaxSequence() {
        return maxSequence;
    }

    public long getTotalBitsCount() {
        return totalBitsCount;
    }

    public int getBytesCount() {
        return bytesCount;
    }

    public int getTrashBitsCount() {
        return trashBitsCount;
    }
}