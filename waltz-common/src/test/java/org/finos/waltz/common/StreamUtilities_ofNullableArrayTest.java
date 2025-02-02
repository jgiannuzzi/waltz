package org.finos.waltz.common;

import org.junit.Test;

import java.util.stream.Stream;

import static junit.framework.Assert.assertEquals;

public class StreamUtilities_ofNullableArrayTest {
    @Test
    public void simpleOfNullableArray1(){
        String[] elements = {"a", "b"};
        Stream t = StreamUtilities.ofNullableArray(elements);
        assertEquals(2, t.count());
    }

    @Test
    public void simpleOfNullableArray2(){
        String[] elements = null;
        Stream t = StreamUtilities.ofNullableArray(elements);
        assertEquals(0, t.count());
    }
}
