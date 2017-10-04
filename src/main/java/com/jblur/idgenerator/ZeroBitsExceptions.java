package com.jblur.idgenerator;

/**
 * This exception is thrown when the total count of all bits is equal to 0
 */
public class ZeroBitsExceptions extends Exception{
    public ZeroBitsExceptions(){}
    public ZeroBitsExceptions(String message){
        super(message);
    }
}
