package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.antlr.NumberBaseVisitor;
import com.brennaswitzer.cookbook.antlr.NumberLexer;
import com.brennaswitzer.cookbook.antlr.NumberParser;
import com.brennaswitzer.cookbook.util.antlr.FastFailErrorListener;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.NumberFormat;
import java.util.function.BiFunction;

public final class NumberUtils {

    private static final Log logger = LogFactory.getLog(NumberUtils.class);

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
            return "NumberWithRange{" + "number=" + number +
                    ", start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    private static class NumVis extends NumberBaseVisitor<NumberWithRange> {

        @Override
        public NumberWithRange visitStart(NumberParser.StartContext ctx) {
            NumberWithRange nwr = super.visitStart(ctx);
            if (ctx.d == null) {
                return nwr;
            }
            return aggregateResult(
                    val(-1, ctx),
                    nwr,
                    (a, b) -> a * b
            );
        }

        @Override
        public NumberWithRange visitFraction(NumberParser.FractionContext ctx) {
            if (ctx.vf != null) {
                return visitVulgarFraction(ctx.vf);
            }
            return aggregateResult(
                    visitInteger(ctx.n),
                    visitInteger(ctx.d),
                    (n, d) -> n / d
            );
        }

        @Override
        public NumberWithRange visitNumber(NumberParser.NumberContext ctx) {
            if (ctx.f != null) {
                return aggregateResult(visitInteger(ctx.i), visitFraction(ctx.f));
            }
            return super.visitNumber(ctx);
        }

        @Override
        public NumberWithRange visitDecimal(NumberParser.DecimalContext ctx) {
            return val(Double.parseDouble(ctx.getText()), ctx);
        }

        @Override
        public NumberWithRange visitInteger(NumberParser.IntegerContext ctx) {
            return val(Double.parseDouble(ctx.getText()), ctx);
        }

        @Override
        public NumberWithRange visitVulgarFraction(NumberParser.VulgarFractionContext ctx) {
            return val(ctx.val, ctx);
        }

        @Override
        public NumberWithRange visitName(NumberParser.NameContext ctx) {
            return val(ctx.val, ctx);
        }

        @Override
        protected NumberWithRange aggregateResult(NumberWithRange aggregate, NumberWithRange nextResult) {
            return aggregateResult(aggregate, nextResult, Double::sum);
        }

        private NumberWithRange aggregateResult(NumberWithRange aggregate, NumberWithRange nextResult, BiFunction<Double, Double, Double> combiner) {
            if (aggregate == null) return nextResult;
            if (nextResult == null) return aggregate;
            return new NumberWithRange(
                    combiner.apply(aggregate.number, nextResult.number),
                    Math.min(aggregate.start, nextResult.start),
                    Math.max(aggregate.end, nextResult.end)
            );
        }

        private NumberWithRange val(double val, ParserRuleContext ctx) {
            return new NumberWithRange(
                    val,
                    ctx.start.getStartIndex(),
                    ctx.stop.getStopIndex() + 1
            );
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
            NumberWithRange result = new NumVis().visitStart(parser.start());
            return new NumberWithRange(
                    Math.round(result.number * 1000) / 1000.0,
                    result.start,
                    result.end
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
