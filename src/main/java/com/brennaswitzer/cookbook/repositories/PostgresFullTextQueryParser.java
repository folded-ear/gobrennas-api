package com.brennaswitzer.cookbook.repositories;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class PostgresFullTextQueryParser {

    public String parse(String filter) {
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
                terms.addAll(toWords(filter, at));
                at = filter.length();
            } else {
                terms.addAll(toWords(filter, at, idxDelim));
                at = idxDelim + 1;
                idxDelim = filter.indexOf(delim, at);
                if (idxDelim > 0) {
                    terms.add(String.join(" <-> ",
                                          toWords(filter, at, idxDelim)));
                    at = idxDelim + 1;
                }
            }
        }
        return String.join(" | ", terms);
    }

    private List<String> toWords(String text, int start) {
        return toWords(text, start, text.length());
    }

    private List<String> toWords(String text, int start, int end) {
        return Arrays.stream(text.substring(start, end)
                                 .trim()
                                 .split("\\s+"))
            .filter(Predicate.not(String::isBlank))
            .collect(Collectors.toList());
    }

}
