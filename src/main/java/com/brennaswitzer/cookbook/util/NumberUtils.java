package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.antlr.FloatLexer;
import com.brennaswitzer.cookbook.antlr.FloatParser;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class NumberUtils {

    protected static final Log logger = LogFactory.getLog(NumberUtils.class);

    public static Float parseFloat(String str) {
        if (str == null) return null;
        try {
            FloatLexer lexer = new FloatLexer(CharStreams.fromString(str.toLowerCase()));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            FloatParser parser = new FloatParser(tokens);
            parser.setErrorHandler(new BailErrorStrategy());
            FloatParser.StartContext tree = parser.start();
            if (parser.getNumberOfSyntaxErrors() > 0) return null;
            return Math.round(tree.val * 1000) / 1000f;
        } catch (Exception e) {
            logger.error("Failed to parseFloat(" + str + ")", e);
            return null;
        }
    }

}
