#Distributed unique id generator

Unique id generator for distributed systems.  
Generates unique IDs which consists of time bits, uid bits and sequence bits.  
Bits positions depends on IDMode.  
First byte contains highest bits. Left bits are highest bits.  
For example:  
Number 285 (0b0000000100011101) will be presented in byte array 'result' which will contain two bytes:  
 - result[0] will contain number: 1 (0b00000001)  
 - result[1] will contain number: 29 (0b00011101)  
UniqueID can not contain more than 64 bits.  
Time and sequence can contain more than 64 bits but it doesn't make sense.  
    
Time and sequence can contain more than 64 bits but is doesn't make sense as high bits of time and  sequence which are more than 64 bits will contain only 0s.  
Minimum amount of bits for ID is 1. Logical maximum amount of bits for ID is 192 (64 bits of time, 64 bits of unique ID, 64 bits of sequence). You can use more bits for time and sequence if you definitely know that you need so.  
    
Common use case is to use 64 bits which consists of 41 bits for time and other bits are depend of your system. For example:  
 - Twitter Snowflake: 41 bits time, 11 bits machine ID, 12 bits sequence number. (IDMode.TIME_UID_SEQUENCE)  
 - Instagram: 41 bits time, 13 bits machine ID, 10 bits sequence number. (IDMode.TIME_UID_SEQUENCE)