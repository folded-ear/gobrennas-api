package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.antlr.FloatLexer;
import com.brennaswitzer.cookbook.antlr.FloatParser;
import org.antlr.v4.runtime.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.NumberFormat;

public final class NumberUtils {

    protected static final Log logger = LogFactory.getLog(NumberUtils.class);

    public static Float parseFloat(String str) {
        if (str == null) return null;
        str = str.trim();
        if (str.isEmpty()) return null;
        try {
            FloatLexer lexer = new FloatLexer(CharStreams.fromString(str.toLowerCase()));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FloatParser parser = new FloatParser(tokens);
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
            FloatParser.StartContext tree = parser.start();
            return Math.round(tree.val * 1000) / 1000f;
        } catch (Exception e) {
            logger.error("Failed to parseFloat(\"" + str + "\")", e);
            return null;
        }
    }

    public static String formatFloat(Float f) {
        if (f == null) throw new IllegalArgumentException("Can't format the null Float");
        return NumberFormat.getNumberInstance().format(f);
    }

}
