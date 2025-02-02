package org.finos.waltz.common;

import org.junit.Test;

import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MapUtilities_transformKeysTest {

    @Test
    public void simpleTransformKeys() {
        Map original = MapUtilities.newHashMap(1,'a',2,'b');
        Map<Integer, Character> result = MapUtilities.transformKeys(original, x -> Integer.parseInt(x.toString())*2);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(2));
        assertTrue(result.containsKey(4));
    }

    @Test(expected = NullPointerException.class)
    public void simpleTransformKeysWithNullMap() {
        Map original = null;
        MapUtilities.transformKeys(original, x -> Integer.parseInt(x.toString())*2);
    }

    @Test(expected = NullPointerException.class)
    public void simpleTransformKeysWithNullFunction() {
        Map original = MapUtilities.newHashMap(1,'a',2,'b');
        MapUtilities.transformKeys(original, null);
    }

    @Test(expected = NullPointerException.class)
    public void simpleTransformKeysWithAllNull() {
        Map original = null;
        MapUtilities.transformKeys(original, null);
    }

    @Test
    public void simpleTransformKeysWithEmptyMap() {
        Map original = MapUtilities.newHashMap();
        Map<Integer, Character> result = MapUtilities.transformKeys(original, x -> Integer.parseInt(x.toString())*2);
        assertEquals(0, result.size());
    }
}
