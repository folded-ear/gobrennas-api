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

public final class NumberUtils {

    protected static final Log logger = LogFactory.getLog(NumberUtils.class);

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
