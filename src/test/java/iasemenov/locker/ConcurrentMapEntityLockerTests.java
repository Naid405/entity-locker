package iasemenov.locker;

import org.junit.Before;

public class ConcurrentMapEntityLockerTests extends EntityLockerTests {
    @Before
    public void setUp() throws Exception {
        this.setLocker(new ConcurrentMapEntityLocker<>());
    }
}