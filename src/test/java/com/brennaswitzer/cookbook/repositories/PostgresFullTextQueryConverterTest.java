package com.brennaswitzer.cookbook.repositories;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostgresFullTextQueryConverterTest {

    @ParameterizedTest
    @CsvSource(quoteCharacter = '#', // we want to process single quotes
            value = {
                    "chicken                   , chicken",
                    "chicken thighs            , chicken | thighs",
                    "'chicken thighs'          , chicken <-> thighs",
                    "\"chicken thighs\"        , chicken <-> thighs",
                    "celery \"chicken thighs\" , celery | chicken <-> thighs",
                    "'a b' \"c d\" e 'f \"g    , a <-> b | c <-> d | e | f | g"
        })
    public void filterConversion(String input, String expected) {
        String actual = new PostgresFullTextQueryConverter()
                .convert(input);
        assertEquals(expected, actual, String.format("expected '%s' from '%s' but got '%s'",
                                                     expected,
                                                     input,
                                                     actual));
    }

}
