package com.jblur.idgenerator;

/**
 * This error is thrown when total count of all bits is equal to 0
 */
public class ZeroBitsExceptions extends Exception{
    public ZeroBitsExceptions(){}
    public ZeroBitsExceptions(String message){
        super(message);
    }
}
