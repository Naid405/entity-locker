package iasemenov.locker;

/**
 * Reusable utility class that provides synchronization mechanism similar to row-level DB locking.
 * The class is supposed to be used by the components that are responsible for managing storage and caching
 * of different type of entities in the application. EntityLocker itself does not deal with the entities,
 * only with the IDs (primary keys) of the entities.
 * <p>
 * Unlock should only come after successful lock, otherwise it may produce IllegalMonitorStateException.
 * <p>
 * Lock behavior is derived from @see java.util.concurrent.locks.ReentrantLock
 *
 * @param <I> Entity ID type
 */
public interface EntityLocker<I> {
    /**
     * Lock the entity ID interruptibly
     *
     * @param entityId entity ID to lock, should not be null
     * @throws InterruptedException     locking was interrupted
     * @throws IllegalArgumentException in case entity id is null
     */
    void lockEntity(I entityId) throws InterruptedException;

    /**
     * Lock the entity ID interruptibly with the given timeout in nanoseconds
     *
     * @param entityId entity ID to lock, should not be null
     * @throws InterruptedException     locking was interrupted
     * @throws IllegalArgumentException in case entity id is null
     */
    boolean lockEntity(I entityId, long timeoutNanos) throws InterruptedException;

    /**
     * Unlock the entity ID if it is locked, do nothing otherwise
     *
     * @param entityId entity ID to unlock, should not be null
     * @throws IllegalArgumentException in case entity id is null
     */
    void unlockEntity(I entityId);
}
