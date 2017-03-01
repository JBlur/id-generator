# distributed_unique_id_generator
Unique id generator for distributed systems.

Generates an 64 bytes unique id depending on time and instance id.
Each instance can generate up to 1024 unique ids per millisecond.
Maximum total of instances is 8192.
Unique ids will not have collisions 69 years (from your epoch time) in case that you don't use more than 8192 instances and each instance generates no more than 1024 unique ids per millisecond.
If you need to generate more than 1024 ids per millisecond per instance or more instances to generate ids or more years of unique id generation you can extend id with additional bits.
