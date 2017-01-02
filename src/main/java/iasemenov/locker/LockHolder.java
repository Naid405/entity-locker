package iasemenov.locker;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Used to store the lock in HashingEntityLocker
 */
public interface LockHolder {
    ReentrantLock get();
}
