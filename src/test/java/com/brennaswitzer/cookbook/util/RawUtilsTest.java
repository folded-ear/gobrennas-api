package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.payload.RawIngredientDissection;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RawUtilsTest {

    @Test
    public void longestSuffix() {
        assertEquals(0, RawUtils.lengthOfLongestSharedSuffix("", ""));
        assertEquals(0, RawUtils.lengthOfLongestSharedSuffix("abc", "xyz"));
        assertEquals(1, RawUtils.lengthOfLongestSharedSuffix("a", "a"));
        assertEquals(3, RawUtils.lengthOfLongestSharedSuffix("abc", "abc"));
        assertEquals(3, RawUtils.lengthOfLongestSharedSuffix("abc", "zzzzabc"));
        assertEquals(3, RawUtils.lengthOfLongestSharedSuffix("zzzzabc", "abc"));
    }

    @Test
    public void fromTestFile() throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/raw/dissections.txt")));
        r.readLine(); // the header
        r.lines()
                .filter(l -> !l.trim().isEmpty())
                .map(l -> l.split("\\|"))
                .map(this::inflateDissection)
                .forEach(this::testDissection);
    }

    private void testDissection(RawIngredientDissection expected) {
        assertEquals(expected, RawUtils.dissect(expected.getRaw()));
    }

    private RawIngredientDissection inflateDissection(String[] parts) {
        RawIngredientDissection dissection = new RawIngredientDissection(parts[0]);
        if (parts.length > 1 && !parts[1].isEmpty()) {
            dissection.setQuantity(new RawIngredientDissection.Section(
                    parts[1],
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            ));
        }
        if (parts.length > 4 && !parts[4].isEmpty()) {
            dissection.setUnits(new RawIngredientDissection.Section(
                    parts[4],
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6])
            ));
        }
        if (parts.length > 7 && !parts[7].isEmpty()) {
            dissection.setName(new RawIngredientDissection.Section(
                    parts[7],
                    Integer.parseInt(parts[8]),
                    Integer.parseInt(parts[9])
            ));
        }
        return dissection;
    }
}
