package com.jblur.idgenerator;

/**
 * IDMode shows in which sequence bits are presented.<br>
 * For example:<br>
 * {@link IDMode#TIME_UID_SEQUENCE} places time bits as highest bits and sequence bits as lowest bits.
 */
public enum IDMode {
    TIME_UID_SEQUENCE,
    TIME_SEQUENCE_UID,
    UID_TIME_SEQUENCE,
    UID_SEQUENCE_TIME,
    SEQUENCE_TIME_UID,
    SEQUENCE_UID_TIME
}
