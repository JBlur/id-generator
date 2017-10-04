package com.jblur.idgenerator;

/**
 * This exception is thrown when we reach maximum id generations per millisecond
 */
public class SequenceOverflowException extends Exception{
    public SequenceOverflowException(){}
    public SequenceOverflowException(String message){
        super(message);
    }
}
