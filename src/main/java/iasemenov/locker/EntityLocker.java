package iasemenov.locker;

import java.util.concurrent.TimeUnit;

/**
 * Reusable utility class that provides synchronization mechanism similar to row-level DB locking.
 * The class is supposed to be used by the components that are responsible for managing storage and caching
 * of different type of entities in the application. EntityLocker itself does not deal with the entities,
 * only with the IDs (primary keys) of the entities.
 * <code>
 * Should always be used like:
 * locker.lockEntity(id);
 * try {
 * //operations on that entity
 * } finally {
 * locker.unlockEntity(id);
 * }
 * </code>
 * <p>
 * Unlock should only come after successful lock.
 *
 * @param <I> Entity ID type
 */
public interface EntityLocker<I> {
    void lockEntity(I entityId) throws InterruptedException;

    boolean lockEntity(I entityId, long timeout, TimeUnit unit) throws InterruptedException;

    void unlockEntity(I entityId);
}
