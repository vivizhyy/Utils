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
        long longIdToEncode = 2679397;
        System.out.println(Base62IdObfuscator.encode(longIdToEncode));
    }

    @Test
    public void testDecode() {
        System.out.println(Base62IdObfuscator.decode("cJrDn88A"));
    }

}
