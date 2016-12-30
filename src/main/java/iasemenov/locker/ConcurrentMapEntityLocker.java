package iasemenov.locker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * EntityLocker implementation based on concurrent map.
 * <p>
 * Type of concurrent map can be overriden through constructor.
 * Entity ID should have the same contract as the key of designated map.
 * <p>
 * This implementation guarantees that freed-up locks could be collected by GC.
 *
 * @param <I> Entity ID type
 */
public class ConcurrentMapEntityLocker<I> implements EntityLocker<I> {
    private final ConcurrentMap<I, ReentrantLock> locksMap;

    /**
     * Uses @see java.util.concurrent.ConcurrentHashMap for id-lock mapping
     */
    public ConcurrentMapEntityLocker() {
        this.locksMap = new ConcurrentHashMap<>();
    }

    /**
     * @param locksMap ConcurrentMap to use for id-lock mapping
     */
    public ConcurrentMapEntityLocker(ConcurrentMap<I, ReentrantLock> locksMap) {
        this.locksMap = locksMap;
    }

    @Override
    public void lockEntity(I entityId) throws InterruptedException {
        boolean lockedInMap = false;
        //Get the lock, lock on it and check if it is still in the map
        //because it may have been removed by unlock while we locked
        do {
            ReentrantLock lock = getMappedLock(entityId);
            lock.lockInterruptibly();
            if (lock == getMappedLock(entityId)) {
                lockedInMap = true;
            } else {
                lock.unlock();
            }
        } while (!lockedInMap);
    }

    @Override
    public boolean lockEntity(I entityId, long timeoutNanos) throws InterruptedException {
        boolean lockedInMap = false;
        long finalNanos = System.nanoTime() + timeoutNanos;
        do {
            ReentrantLock lock = getMappedLock(entityId);
            if (!lock.tryLock(finalNanos - System.nanoTime(), TimeUnit.NANOSECONDS)) {
                return false;
            }
            if (lock == getMappedLock(entityId)) {
                lockedInMap = true;
            } else {
                lock.unlock();
            }
        } while (!lockedInMap);

        return true;
    }

    private ReentrantLock getMappedLock(I entityId) {
        ReentrantLock newLock = new ReentrantLock();
        ReentrantLock lock = locksMap.putIfAbsent(entityId, newLock);
        return lock == null ? newLock : lock;
    }

    @Override
    public void unlockEntity(I entityId) {
        ReentrantLock lock = locksMap.get(entityId);
        //Remove the lock from map if no threads are waiting on it to conserve memory
        if (lock != null) {
            if (!lock.hasQueuedThreads()) {
                locksMap.remove(entityId);
            }
            lock.unlock();
        }
    }
}
