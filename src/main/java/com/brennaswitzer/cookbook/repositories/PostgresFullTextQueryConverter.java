package com.brennaswitzer.cookbook.repositories;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * I convert a user-entered search string into a string suitable for passing to
 * Postgres' <tt>TO_TSQUERY</tt> function.
 */
@Service
public class PostgresFullTextQueryConverter {

    public String convert(String filter) {
        List<String> terms = new ArrayList<>();
        int at = 0;
        while (at < filter.length()) {
            char delim = '"';
            int idxDelim = filter.indexOf(delim, at);
            int idxApostrophe = filter.indexOf('\'', at);
            if (idxDelim < 0 || (idxApostrophe >= 0 && idxApostrophe < idxDelim)) {
                delim = '\'';
                idxDelim = idxApostrophe;
            }
            if (idxDelim < 0) {
                terms.addAll(toWords(filter, at, filter.length(), true));
                at = filter.length();
            } else {
                terms.addAll(toWords(filter, at, idxDelim, true));
                at = idxDelim + 1;
                idxDelim = filter.indexOf(delim, at);
                if (idxDelim > 0) {
                    terms.add(String.join(" <-> ",
                                          toWords(filter, at, idxDelim, false)));
                    at = idxDelim + 1;
                }
            }
        }
        return String.join(" | ", terms);
    }

    private List<String> toWords(String text, int start, int end, boolean prefix) {
        Stream<String> words = Arrays.stream(text.substring(start, end)
                                                     .trim()
                                                     .split("\\s+"))
                .filter(Predicate.not(String::isBlank));
        if (prefix) {
            words = words.map(w -> w + ":*");
        }
        return words.collect(Collectors.toList());
    }

}
