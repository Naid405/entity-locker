package iasemenov.locker;

import java.util.concurrent.locks.ReentrantLock;

public class EagerLockHolderFactory implements LockHolderFactory {
    public LockHolder create(boolean fairLock) {
        return new EagerLockHolder(fairLock);
    }

    private class EagerLockHolder implements LockHolder {
        private final ReentrantLock lock;

        public EagerLockHolder(boolean fairLock) {
            lock = new ReentrantLock(fairLock);
        }

        @Override
        public ReentrantLock get() {
            return lock;
        }
    }
}
