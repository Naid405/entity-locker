package iasemenov.locker;

import org.junit.Before;

public class HashingEntityLockerTests extends EntityLockerTests {
    @Before
    public void setUp() throws Exception {
        this.setLocker(new HashingEntityLocker<>(10, 1));
    }
}