package com.jblur.idgenerator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.xml.bind.DatatypeConverter;

/**
 * Unique String ID generator for distributed environments.
 */
public class StringIDGenerator extends IDGenerator {
    public StringIDGenerator(long epochStartTime, long uniqueID, int timeBitsCount, int uniqueIdBitsCount, int sequenceBitsCount, IDMode idMode) throws ZeroBitsExceptions, NegativeBitsCountException {
        super(epochStartTime, uniqueID, timeBitsCount, uniqueIdBitsCount, sequenceBitsCount, idMode);
    }

    public String generateStringId() throws SequenceOverflowException{
        return new String(generateId());
    }

    public String generateHexBinaryId() throws SequenceOverflowException{
        return DatatypeConverter.printHexBinary(generateId());
    }

    public String generateBase64Id() throws SequenceOverflowException{
        return new String(Base64.getEncoder().encode(generateId()));
    }

    public String generateURLEncodedId() throws SequenceOverflowException, UnsupportedEncodingException {
        return URLEncoder.encode(generateStringId(), StandardCharsets.US_ASCII.toString());
    }
}
