package iasemenov.locker;

import java.util.concurrent.locks.ReentrantLock;

public interface LockHolder {
    ReentrantLock get();
}
