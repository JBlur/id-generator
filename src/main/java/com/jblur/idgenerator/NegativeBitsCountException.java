package com.jblur.idgenerator;

/**
 * This exception is thrown when we provide negative bits count to and of ID generators
 */
public class NegativeBitsCountException extends Exception {
    public NegativeBitsCountException(){}
    public NegativeBitsCountException(String message){
        super(message);
    }
}
