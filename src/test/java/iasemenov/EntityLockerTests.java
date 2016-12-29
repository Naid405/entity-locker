package iasemenov;

import iasemenov.locker.EntityLocker;
import iasemenov.locker.EntityLockerImpl;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class EntityLockerTests {
    private Logger logger = Logger.getLogger(EntityLockerTests.class);

    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private EntityLocker<Long> locker;

    @Before
    public void setUp() throws Exception {
        locker = new EntityLockerImpl<>(2, 1);
    }

    @Test
    public void testOneThreadOneEntityLockUnlock() throws Exception {
        Long id = 1L;

        try {
            locker.lockEntity(id);
        } finally {
            locker.unlockEntity(id);
        }
    }

    @Test
    public void testTwoThreadsOneEntityNoExceptionsOnWait() throws Exception {
        Long id = 1L;
        Runnable runnable = () -> {
            try {
                locker.lockEntity(id);
                Thread.sleep(20);
            } catch (InterruptedException e) {
                logger.fatal("Should not have happened", e);
            } finally {
                locker.unlockEntity(id);
            }
        };
        Future task1 = executor.submit(runnable);
        Future task2 = executor.submit(runnable);

        task1.get();
        task2.get();
    }

    @Test
    public void testLockOtherThreadWaiting() throws Exception {
        Long id = 1L;

        try {
            locker.lockEntity(id);
            Future task1 = executor.submit(() -> {
                try {
                    locker.lockEntity(id);
                } catch (InterruptedException e) {
                    logger.fatal("Should not have happened", e);
                } finally {
                    locker.unlockEntity(id);
                }
            });
            locker.unlockEntity(id);

            task1.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.fatal("Should not have happened", e);
            locker.unlockEntity(id);
        }
    }

    @Test
    public void testLockDifferentIds() throws Exception {
        Long id1 = 1L;
        Long id2 = 2L;

        try {
            locker.lockEntity(id1);
            Future task1 = executor.submit(() -> {
                try {
                    locker.lockEntity(id2);
                } catch (InterruptedException e) {
                    logger.fatal("Should not have happened", e);
                } finally {
                    locker.unlockEntity(id2);
                }
            });

            task1.get(10, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.fatal("Should not have happened", e);
        } finally {
            locker.unlockEntity(id1);
        }
    }

    @Test
    public void testLockTimeout() throws Exception {
        Long id = 1L;

        try {
            locker.lockEntity(id);
            Future<Boolean> task1 = executor.submit(() -> {
                try {
                    return locker.lockEntity(id, 100, TimeUnit.MILLISECONDS);
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

        try {
            locker.lockEntity(id);
            locker.lockEntity(id, 10, TimeUnit.MILLISECONDS);

            Future<Boolean> task1 = executor.submit(() -> {
                try {
                    return locker.lockEntity(id, 100, TimeUnit.MILLISECONDS);
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
}