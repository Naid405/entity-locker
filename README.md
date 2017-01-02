EntityLocker assignment
============

The task is to create a reusable utility class that provides synchronization mechanism similar to row-level DB locking.

The class is supposed to be used by the components that are responsible for managing storage and caching of different type of entities in the application. EntityLocker itself does not deal with the entities, only with the IDs (primary keys) of the entities.

Requirements:

1. EntityLocker should support different types of entity IDs.

2. EntityLocker’s interface should allow the caller to specify which entity does it want to work with (using entity ID), and designate the boundaries of the code that should have exclusive access to the entity (called “protected code”).

3. For any given entity, EntityLocker should guarantee that at most one thread executes protected code on that entity. If there’s a concurrent request to lock the same entity, the other thread should wait until the entity becomes available.

4. EntityLocker should allow concurrent execution of protected code on different entities.



Bonus requirements (optional):

I. Allow reentrant locking.

II. Allow the caller to specify timeout for locking an entity.

III. Implement protection from deadlocks (but not taking into account possible locks outside EntityLocker).

Implementation
--------------
Current implementation provides two variants:

1. iasemenov.locker.HashingEntityLocker - first prototype based on the same idea as Doug Lea's ConcurrentHashMap - locking key segments.
Segmentation is based on hashing entity ID's.
This implementation will degrade to a single mutex in case of poorly implemented hashCode() for entity ID, or if the number of segments is too small,
but it may be a bit faster then the second implementation in case of low number of threads operating on a highly distributed (in terms of hash values) IDs.

2. iasemenov.locker.ConcurrentMapEntityLocker - based on using ConcurrentMap to map entity ID to certain lock.
Locks are removed from map upon unlock if no other thread is trying to lock on it.

Both implementations allow lock reentry and "timeouted" locking. None of them provide deadlock prevention.

