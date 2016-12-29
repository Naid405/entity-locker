package iasemenov.locker;

public interface LockHolderFactory {
    LockHolder create(boolean fairLock);
}
