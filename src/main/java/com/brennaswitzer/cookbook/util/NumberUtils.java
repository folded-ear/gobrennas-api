package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.antlr.NumberLexer;
import com.brennaswitzer.cookbook.antlr.NumberParser;
import org.antlr.v4.runtime.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.NumberFormat;

public final class NumberUtils {

    protected static final Log logger = LogFactory.getLog(NumberUtils.class);

    public static Double parseNumber(String str) {
        if (str == null) return null;
        str = str.trim();
        if (str.isEmpty()) return null;
        try {
            NumberLexer lexer = new NumberLexer(CharStreams.fromString(str.toLowerCase()));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            NumberParser parser = new NumberParser(tokens);
            parser.setErrorHandler(new BailErrorStrategy());
            parser.addErrorListener(new BaseErrorListener() {
                @Override
                public void syntaxError(
                        Recognizer<?, ?> recognizer,
                        Object offendingSymbol,
                        int line,
                        int charPositionInLine,
                        String msg, RecognitionException e
                ) {
                    // any syntax error is immediate failure; we're boolean here
                    throw e;
                }
            });
            NumberParser.StartContext tree = parser.start();
            return Math.round(tree.val * 1000) / 1000.0;
        } catch (Exception e) {
            logger.error("Failed to parseNumber(\"" + str + "\")", e);
            return null;
        }
    }

    public static String formatNumber(Double n) {
        if (n == null) throw new IllegalArgumentException("Can't format the null Float");
        return NumberFormat.getNumberInstance().format(n);
    }

}
