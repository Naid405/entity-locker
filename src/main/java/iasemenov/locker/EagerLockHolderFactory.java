package iasemenov.locker;

import java.util.concurrent.locks.ReentrantLock;

public class EagerLockHolderFactory implements LockHolderFactory {
    public LockHolder create(boolean fairLock) {
        return new EagerLockHolder(fairLock);
    }

    private class EagerLockHolder implements LockHolder {
        private final ReentrantLock lock;
        private final boolean fairLock;

        public EagerLockHolder(boolean fairLock) {
            this.fairLock = fairLock;
            lock = new ReentrantLock(fairLock);
        }

        @Override
        public ReentrantLock get() {
            return lock;
        }
    }
}
