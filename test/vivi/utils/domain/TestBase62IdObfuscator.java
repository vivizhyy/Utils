package vivi.utils.domain;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestBase62IdObfuscator {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testEncode() {
        long longIdToEncode = 6910223;
        System.out.println(Base62IdObfuscator.encode(longIdToEncode));
    }

    @Test
    public void testDecode() {
        long decodedLong = Base62IdObfuscator.decode("z95O3FpFz");
        System.out.println(decodedLong);
    }

}
