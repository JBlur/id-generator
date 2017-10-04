package com.jblur.idgenerator;

/**
 * This exception is thrown when total count of bits doesn't reach required bits' count.<br>
 * For example:<br>
 * If we use LongIDGenerator we must provide exactly 64 bits. In case out total bits count
 * (timeBitsCount + uniqueIdBitsCount + sequenceBitsCount) doesn't much 64 bits we weill be shown this exception.
 */
public class BitsCountException extends Exception{
    public BitsCountException(){}
    public BitsCountException(String message){
        super(message);
    }
}
