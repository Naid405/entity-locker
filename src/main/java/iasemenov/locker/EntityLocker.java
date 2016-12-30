package iasemenov.locker;

/**
 * Reusable utility class that provides synchronization mechanism similar to row-level DB locking.
 * The class is supposed to be used by the components that are responsible for managing storage and caching
 * of different type of entities in the application. EntityLocker itself does not deal with the entities,
 * only with the IDs (primary keys) of the entities.
 * <p>
 * Unlock should only come after successful lock, otherwise it may produce IllegalMonitorStateException.
 *
 * Lock behavior is derived from @see java.util.concurrent.locks.ReentrantLock
 *
 * @param <I> Entity ID type
 */
public interface EntityLocker<I> {
    void lockEntity(I entityId) throws InterruptedException;

    boolean lockEntity(I entityId, long timeoutNanos) throws InterruptedException;

    void unlockEntity(I entityId);
}
