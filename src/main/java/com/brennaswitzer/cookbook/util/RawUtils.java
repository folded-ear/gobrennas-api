package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.payload.RawIngredientDissection;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RawUtils {

    private static final char SPACE = ' ';

    public static int lengthOfLongestSharedSuffix(CharSequence a, CharSequence b) {
        int ai = a.length() - 1, bi = b.length() - 1;
        while (ai >= 0 && bi >= 0 && a.charAt(ai) == b.charAt(bi)) {
            ai--;
            bi--;
        }
        return a.length() - ai - 1;
    }

    public static int lastIndexOfNameStart(String raw) {
        return lastIndexOfNameStart(raw, raw.length());
    }

    public static int lastIndexOfNameStart(String raw, int fromIndex) {
        return IntStream.builder()
                .add(raw.lastIndexOf('"', fromIndex))
                .add(raw.lastIndexOf('“', fromIndex))
                .add(raw.lastIndexOf('”', fromIndex))
                .add(raw.lastIndexOf('«', fromIndex))
                .add(raw.lastIndexOf('»', fromIndex))
                .build()
                .filter(i -> i >= 0)
                .max()
                .orElse(-1);
    }

    public static boolean containsNameDelim(String raw) {
        return raw.indexOf('"') > 0 ||
               raw.indexOf('“') > 0 ||
               raw.indexOf('”') > 0 ||
               raw.indexOf('«') > 0 ||
               raw.indexOf('»') > 0;
    }

    public static String stripMarkers(String region) {
        if (region == null) return region;
        if (region.length() < 3) return region;
        char c = region.charAt(0);
        char end = switch (c) {
            case '“' -> '”';
            case '«' -> '»';
            default -> c;
        };
        if (end != region.charAt(region.length() - 1)) return region;
        if (Character.isLetterOrDigit(c)) return region;
        return region.substring(1, region.length() - 1);
    }

    public static RawIngredientDissection dissect(String raw) {
        if (ValueUtils.noValue(raw)) return null;
        RawIngredientDissection d = new RawIngredientDissection(raw);
        NumberUtils.NumberWithRange n = NumberUtils.parseNumberWithRange(raw);
        int pos = 0;
        if (n != null) {
            d.setQuantity(new RawIngredientDissection.Section(
                    raw.substring(n.start(), n.end()),
                    n.start(),
                    n.end()
            ));
            pos = n.end();
        }
        RawIngredientDissection.Section s = findSection(raw, pos, '_', '_');
        if (s != null) {
            d.setUnits(s);
        }
        Stream.<RawIngredientDissection.Section>builder()
                .add(findSection(raw, pos, '"', '"'))
                .add(findSection(raw, pos, '“', '”'))
                .add(findSection(raw, pos, '«', '»'))
                .build()
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(RawIngredientDissection.Section::getStart))
                .ifPresent(d::setName);
        return d;
    }

    private static RawIngredientDissection.Section findSection(String str, int pos, char startDelim, char endDelim) {
        if (pos > 0 && str.charAt(pos - 1) == SPACE) pos -= 1;
        int start = str.indexOf(startDelim, pos);
        if (start < 0) return null;
        int end = str.indexOf(endDelim, start + 1);
        if (end < 0) return null;
        return new RawIngredientDissection.Section(
                str.substring(start + 1, end + 1 - 1),
                start,
                end + 1
        );
    }

}
