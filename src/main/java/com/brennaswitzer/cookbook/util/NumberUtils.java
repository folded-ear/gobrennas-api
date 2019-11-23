package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.antlr.NumberLexer;
import com.brennaswitzer.cookbook.antlr.NumberParser;
import com.brennaswitzer.cookbook.util.antlr.FastFailErrorListener;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.NumberFormat;
import java.util.Map;
import java.util.TreeMap;

public final class NumberUtils {

    protected static final Log logger = LogFactory.getLog(NumberUtils.class);

    public static Map<String, Double> NAMES;

    static {
        // HashMap'd be faster, but case insensitivity is required
        NAMES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        NAMES.put("one half" ,  0.5);
        NAMES.put("half"     ,  0.5);
        NAMES.put("one"      ,  1.0);
        NAMES.put("two"      ,  2.0);
        NAMES.put("three"    ,  3.0);
        NAMES.put("four"     ,  4.0);
        NAMES.put("five"     ,  5.0);
        NAMES.put("six"      ,  6.0);
        NAMES.put("seven"    ,  7.0);
        NAMES.put("eight"    ,  8.0);
        NAMES.put("nine"     ,  9.0);
        NAMES.put("ten"      , 10.0);
        NAMES.put("eleven"   , 11.0);
        NAMES.put("twelve"   , 12.0);
        NAMES.put("thirteen" , 13.0);
        NAMES.put("fourteen" , 14.0);
        NAMES.put("fifteen"  , 15.0);
        NAMES.put("sixteen"  , 16.0);
        NAMES.put("seventeen", 17.0);
        NAMES.put("eighteen" , 18.0);
        NAMES.put("nineteen" , 19.0);
        NAMES.put("twenty"   , 20.0);

        NAMES.put("¼", 1.0 / 4.0);
        NAMES.put("½", 1.0 / 2.0);
        NAMES.put("¾", 3.0 / 4.0);
        NAMES.put("⅐", 1.0 / 7.0);
        NAMES.put("⅑", 1.0 / 9.0);
        NAMES.put("⅒", 1.0 / 10.0);
        NAMES.put("⅓", 1.0 / 3.0);
        NAMES.put("⅔", 2.0 / 3.0);
        NAMES.put("⅕", 1.0 / 5.0);
        NAMES.put("⅖", 2.0 / 5.0);
        NAMES.put("⅗", 3.0 / 5.0);
        NAMES.put("⅘", 4.0 / 5.0);
        NAMES.put("⅙", 1.0 / 6.0);
        NAMES.put("⅚", 5.0 / 6.0);
        NAMES.put("⅛", 1.0 / 8.0);
        NAMES.put("⅜", 3.0 / 8.0);
        NAMES.put("⅝", 5.0 / 8.0);
        NAMES.put("⅞", 7.0 / 8.0);
    }

    public static class NumberWithRange {
        private final double number;
        private final int start;
        private final int end;

        public NumberWithRange(double number, int start, int end) {
            this.number = number;
            this.start = start;
            this.end = end;
        }

        public double getNumber() {
            return number;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("NumberWithRange{");
            sb.append("number=").append(number);
            sb.append(", start=").append(start);
            sb.append(", end=").append(end);
            sb.append('}');
            return sb.toString();
        }
    }

    public static NumberWithRange parseNumberWithRange(String str) {
        if (str == null) return null;
        if (str.trim().isEmpty()) return null;
        try {
            NumberLexer lexer = new NumberLexer(CharStreams.fromString(str.toLowerCase()));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            NumberParser parser = new NumberParser(tokens);
            parser.setErrorHandler(new BailErrorStrategy());
            parser.addErrorListener(new FastFailErrorListener());
            NumberParser.StartContext tree = parser.start();
            return new NumberWithRange(
                    Math.round(tree.val * 1000) / 1000.0,
                    tree.start.getStartIndex(),
                    tree.stop.getStopIndex() + 1 // we want end, not stop
            );
        } catch (Exception e) {
            logger.info("Failed to parseNumber(\"" + str + "\")");
            return null;
        }
    }

    public static Double parseNumber(String str) {
        return parseNumber(str, false);
    }

    public static Double parseNumber(String str, boolean allowGarbageAfter) {
        NumberWithRange numberWithRange = parseNumberWithRange(str);
        if (numberWithRange == null) return null;
        if (allowGarbageAfter) return numberWithRange.number;
        if (numberWithRange.end != str.length()) return null;
        return numberWithRange.number;
    }

    public static String formatNumber(Double n) {
        if (n == null) throw new IllegalArgumentException("Can't format the null Float");
        return NumberFormat.getNumberInstance().format(n);
    }

}
