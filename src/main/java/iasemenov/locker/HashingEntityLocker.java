package iasemenov.locker;

import iasemenov.util.Assert;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EntityLocker implementation based on separating entity IDs into segments by their hash and locking segments.
 * Entity ID should correctly and effectively implement @see Object.hashCode() to reduce the chance of locking big segments.
 *
 * @param <I> Entity ID type
 */
public class HashingEntityLocker<I> implements EntityLocker<I> {
    private final LockHolder[] lockArray;
    private final int lockArraySize;
    private final LockHolderFactory lockHolderFactory;

    /**
     * Parameters are used to determine the size of lock array.
     * Higher contention factor -> higher the chance to lock multiple entities.
     *
     * @param approximateNumberOfEntities approximate number of entities supposed to be handled by this EntityLocker
     * @param lockContentionFactor        number of entities under single lock
     */
    public HashingEntityLocker(int approximateNumberOfEntities, int lockContentionFactor) {
        Assert.moreTheZero(approximateNumberOfEntities, "Approximate number of entities should be more then 0");
        Assert.moreTheZero(lockContentionFactor, "Lock contention factor should be more then 0");

        this.lockArraySize = Math.max(1, approximateNumberOfEntities / lockContentionFactor);
        this.lockHolderFactory = new EagerLockHolderFactory();
        this.lockArray = initArray(lockArraySize);
    }

    /**
     * Parameters are used to determine the size of lock array.
     * Higher contention factor -> higher the chance to lock multiple entities.
     *
     * @param approximateNumberOfEntities approximate number of entities supposed to be handled by this EntityLocker
     * @param lockContentionFactor        number of entities under single lock
     * @param lockHolderFactory           factory for id lock holders
     */
    public HashingEntityLocker(int approximateNumberOfEntities, int lockContentionFactor, LockHolderFactory lockHolderFactory) {
        Assert.moreTheZero(approximateNumberOfEntities, "Approximate number of entities should be more then 0");
        Assert.moreTheZero(lockContentionFactor, "Lock contention factor should be more then 0");
        Assert.notNull(lockHolderFactory, "Lock supplier cannot be null");

        this.lockArraySize = Math.max(1, approximateNumberOfEntities / lockContentionFactor);
        this.lockHolderFactory = lockHolderFactory;
        this.lockArray = initArray(lockArraySize);
    }

    protected LockHolder[] initArray(int arraySize) {
        Assert.moreTheZero(arraySize, "Array size should be more then 0");
        LockHolder[] lockArray = new LockHolder[arraySize];
        for (int i = 0; i < arraySize; i++) {
            lockArray[i] = lockHolderFactory.create(true);
        }

        return lockArray;
    }

    @Override
    public void lockEntity(I entityId) throws InterruptedException {
        Assert.notNull(entityId, "Entity ID cannot be null");
        ReentrantLock lock = lockArray[(lockArraySize - 1) & hash(entityId)].get();
        lock.lockInterruptibly();
    }

    @Override
    public boolean lockEntity(I entityId, long timeoutNanos) throws InterruptedException {
        Assert.notNull(entityId, "Entity ID cannot be null");
        ReentrantLock lock = lockArray[(lockArraySize - 1) & hash(entityId)].get();
        return lock.tryLock(timeoutNanos, TimeUnit.NANOSECONDS);
    }

    @Override
    public void unlockEntity(I entityId) {
        Assert.notNull(entityId, "Entity ID cannot be null");
        ReentrantLock lock = lockArray[(lockArraySize - 1) & hash(entityId)].get();
        try {
            lock.unlock();
        } catch (IllegalMonitorStateException ignored) {
            //Suppress the exception to conform with interface contract
        }
    }

    /**
     * Hash distribution algorithm, got it from @see java.util.HashMap
     *
     * @param entityId id to hash
     * @return id hash
     */
    protected int hash(I entityId) {
        int h;
        return (entityId == null) ? 0 : (h = entityId.hashCode()) ^ (h >>> 16);
    }
}
