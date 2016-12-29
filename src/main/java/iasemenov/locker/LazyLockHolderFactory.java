package iasemenov.locker;

import java.util.concurrent.locks.ReentrantLock;

public class LazyLockHolderFactory implements LockHolderFactory {
    public LockHolder create(boolean fairLock) {
        return new EagerLockHolder(fairLock);
    }

    private class EagerLockHolder implements LockHolder {
        private volatile ReentrantLock lock;
        private final boolean fairLock;

        public EagerLockHolder(boolean fairLock) {
            this.fairLock = fairLock;
        }

        @Override
        public ReentrantLock get() {
            if (lock == null) {
                synchronized (this) {
                    if (lock == null) {
                        lock = new ReentrantLock(fairLock);
                    }
                }
            }
            return lock;
        }
    }
}
