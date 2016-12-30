package iasemenov.locker;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;

public abstract class EntityLockerTests {
    private Logger logger = Logger.getLogger(EntityLockerTests.class);

    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private EntityLocker<Long> locker;

    /**
     * Subclasses should call this prior to test execution
     *
     * @param locker entity locker to use for tests
     */
    public void setLocker(EntityLocker<Long> locker) {
        this.locker = locker;
    }

    @After
    public void tearDown() throws Exception {
        //Otherwise threads will pile up
        executor.shutdown();
    }

    @Test
    public void testOneThreadOneEntityLockUnlock() throws Exception {
        Long id = 1L;

        locker.lockEntity(id);
        locker.unlockEntity(id);
    }

    @Test
    public void testTwoThreadsOneEntityNoExceptionsOnWait() throws Exception {
        Long id = 1L;
        Runnable runnable = () -> {
            try {
                locker.lockEntity(id);
                locker.unlockEntity(id);
            } catch (InterruptedException e) {
                logger.fatal("Should not have happened", e);
            }
        };
        Future task1 = executor.submit(runnable);
        Future task2 = executor.submit(runnable);

        task1.get();
        task2.get();
    }

    @Test(expected = TimeoutException.class)
    public void testLockOtherThreadWaiting() throws Exception {
        Long id = 1L;

        locker.lockEntity(id);
        Future task1 = executor.submit(() -> {
            try {
                locker.lockEntity(id);
            } catch (InterruptedException e) {
                //Expected to happen
            }
            locker.unlockEntity(id);
        });

        try {
            task1.get(10, TimeUnit.MILLISECONDS);
        } finally {
            task1.cancel(true);
            locker.unlockEntity(id);
        }
    }

    @Test
    public void testLockDifferentIds() throws Exception {
        Long id1 = 1L;
        Long id2 = 2L;

        locker.lockEntity(id1);

        Future task1 = executor.submit(() -> {
            try {
                locker.lockEntity(id2);
                locker.unlockEntity(id2);
            } catch (InterruptedException e) {
                logger.fatal("Should not have happened", e);
            }
        });

        task1.get(50, TimeUnit.MILLISECONDS);

        locker.unlockEntity(id1);
    }

    @Test
    public void testLockTimeout() throws Exception {
        Long id = 1L;

        locker.lockEntity(id);
        try {
            Future<Boolean> task1 = executor.submit(() -> {
                try {
                    boolean locked = locker.lockEntity(id, 100000);
                    if (locked) locker.unlockEntity(id);
                    return locked;
                } catch (InterruptedException e) {
                    logger.fatal("Should not have happened", e);
                    return true;
                }
            });
            Assert.assertFalse(task1.get());
        } catch (InterruptedException e) {
            logger.fatal("Should not have happened", e);
        } finally {
            locker.unlockEntity(id);
        }
    }

    @Test
    public void testLockReentry() throws Exception {
        Long id = 1L;

        locker.lockEntity(id);
        locker.lockEntity(id, 10);

        Future<Boolean> task1 = executor.submit(() -> {
            try {
                return locker.lockEntity(id, 100000);
            } catch (InterruptedException e) {
                logger.fatal("Should not have happened", e);
                return true;
            }
        });
        Assert.assertFalse(task1.get());

        locker.unlockEntity(id);
    }
}