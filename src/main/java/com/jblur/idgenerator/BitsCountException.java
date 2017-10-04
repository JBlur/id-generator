package com.jblur.idgenerator;

/**
 * This exception is thrown when the total count of bits doesn't reach the required count of bits.<br>
 * For example:<br>
 * If we use LongIDGenerator we must provide exactly 64 bits. In case our total bits count
 * (timeBitsCount + uniqueIdBitsCount + sequenceBitsCount) doesn't much 64 bits we will be shown this exception.
 */
public class BitsCountException extends Exception{
    public BitsCountException(){}
    public BitsCountException(String message){
        super(message);
    }
}
