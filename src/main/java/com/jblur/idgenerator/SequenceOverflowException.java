package com.jblur.idgenerator;

public class SequenceOverflowException extends Exception{
    public SequenceOverflowException(){}
    public SequenceOverflowException(String message){
        super(message);
    }
}
