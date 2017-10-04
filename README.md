# Unique id generator for distributed systems
### This library allows to generate unique identifiers in distributed systems.

ID types are configurable to meet almost any needs for time based distributed id generation.<br>
Each generated identifier can be in size from 1 to 192 bits. Theoretically, you are not limited to 192 bits, but in practice it doesn't make sense. So, you can generate ids of almost unlimited size, but make sure you understand what you are doing.

As Java doesn't have a primitive type to store 1 bit all ids are packaged into byte arrays. From there it is possible to retrieve generated necessary bits.

For convenience, this library includes id generators for the next types:<br>
byte - `ByteIDGenerator`<br>
short - `ShortIDGenerator`<br>
int - `IntIDGenerator`<br>
long - `LongIDGenerator`<br>
BitSet - `BitSetIDGenerator`<br>
String - `StringIDGenerator`<br>
byte[] - `IDGenerator`<br>

Those id generators allow to retrieve the ID of needed type.<br>
Those id generators are all wrappers around basic IDGenerator (which returns byte arrays of needed size).

Each unique id may contain (but not restricted) time bits, unique instance id bits and sequence bits. It allows to generate unique ids which are sorted by time / instance id or sequence.

Also, each IDGenerator contains `epochStartTime`. It is a time from which epoch of a project is started.<br>
What does it mean?<br>
For example, if we need to generate time based ids in distributed environment, we want to be sure that all our generated ids are unique as long as possible.<br>
Let's assume that we need to generate 64 bits ids which contains 41 bits of time, 10 bits of instance id and 13 bits of sequence (it is one of the most common practice).<br>
With those options we can generate ids which are guaranteed to be unique next 2199023255552 milliseconds (almost for 70 years). Also, we can generate up to 8192 IDs per millisecond on each of 1024 instances (servers). It means that we can generate up to 8388608 unique IDs per millisecond in total for the next 70 years (it is 18446744073709551616 theoretically possible unique ids). And all of that fits in 64 bits (`long` in Java).<br>
To be able to fit 2199023255552 milliseconds in 41 bits we need to set `epochStartTime` which is equal to the current time in milliseconds. It means that we start counting milliseconds not from current time (which is about 1507141731000 at the time this README was created) but from 0.<br>
The practical advice is to choose any time of the year you are launching your project and use this time as `epochStartTime` for all instances forever.<br>
By using IDMode it is possible to change bits order (sort by time, instance id, sequence).

All ID generators are thread safe.

In any generated ID first byte contains highest bits. Left bits are highest bits.<br>
For example:<br>
Number 285 (0b0000000100011101) will be presented in the byte array 'result' which will contain two bytes:  
 - result[0] will contain number: 1 (0b00000001)  
 - result[1] will contain number: 29 (0b00011101)  
    
Common use case is to use 64 bits which consist of 41 bits of time and other bits are depend on your system. For example:   
 - Twitter Snowflake: 41 bits time, 11 bits machine ID, 12 bits sequence number. (IDMode.TIME_UID_SEQUENCE)  
 - Instagram: 41 bits time, 13 bits machine ID, 10 bits sequence number. (IDMode.TIME_UID_SEQUENCE)
 
# Examples:
1. Create long id generator which was described (41 bits of time, 10 of instance, 13 of the sequence):
   ```
   LongIDGenerator longIdGenerator = new LongIDGenerator(1507141731000L, 0, 41, 10, 13, IDMode.TIME_UID_SEQUENCE);
   long myUnqueId1 = longIdGenerator.generateLongId();
   long myUniqueId2 = longIdGenerator.generateLongId();
   ```
2. Create several instances of above id generator:
   ```
   LongIDGenerator longIdGenerator1 = new LongIDGenerator(1507141731000L, 0, 41, 10, 13, IDMode.TIME_UID_SEQUENCE);
   LongIDGenerator longIdGenerator2 = new LongIDGenerator(1507141731000L, 1, 41, 10, 13, IDMode.TIME_UID_SEQUENCE);
   LongIDGenerator longIdGenerator3 = new LongIDGenerator(1507141731000L, 3, 41, 10, 13, IDMode.TIME_UID_SEQUENCE);
   LongIDGenerator longIdGenerator4 = new LongIDGenerator(1507141731000L, 4, 41, 10, 13, IDMode.TIME_UID_SEQUENCE);
   
   long myUnqueId1 = longIdGenerator1.generateLongId();
   long myUniqueId2 = longIdGenerator2.generateLongId();
   long myUniqueId3 = longIdGenerator3.generateLongId();
   long myUniqueId4 = longIdGenerator4.generateLongId();
   ```
   Note that each instance of id generator has to have a unique instance id (in our example it is 1,2,3,4). Make sure that    instance id is unique across all servers.<br>
   Also, it is an uncommon practice to create several instances of same id generator on the same server. As all id generators are    thread safe then it is enough to create one instance of id generator on each server. If you create several instances of the same id generator on the same server because you need to generate more ids per millisecond per server then it may be because bits counts are not set efficiently. Maybe, it is better to increase sequance bit count. But sometimes it may be a good practice to use several id generators on the same server if project architecture requires so.
3. Create integer ID generator which can generate only 1 unique id per millisecond for the next 49.7 days:
   ```
   IntIDGenerator intIdGenerator = new IntIDGenerator(1507141731000L, 0, 32, 0, 0, IDMode.TIME_UID_SEQUENCE);
   int myUniqueId = intIdGenerator.generateIntId();
   ```
4. Create 27 bits ID generator which generates ids where sequence bits are higher bits:
   ```
   IDGenerator idGenerator = new IDGenerator(1501936765671L, 0, 20, 2, 5, IDMode.SEQUENCE_UID_TIME);
   byte[] myUniqueId = idGenerator.generateId();
   ```
5. Create string ID generator:
   ```
   StringIDGenerator stringIDGenerator = new StringIDGenerator(1507141731000L, 0, 50, 20, 10, IDMode.TIME_UID_SEQUENCE);
   
   String simpleStringId = stringIDGenerator.generateStringId(); //may include invisible characters
   String hexStringId = stringIDGenerator.generateHexBinaryId();
   String base64StringId = stringIDGenerator.generateBase64Id();
   String urlStringId = stringIDGenerator.generateURLEncodedId();
   ```
