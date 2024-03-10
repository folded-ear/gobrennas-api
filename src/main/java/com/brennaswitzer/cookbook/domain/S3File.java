package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.regex.Pattern;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class S3File {

    private static final Pattern FILENAME_SANITIZER = Pattern.compile("[^a-zA-Z0-9.\\-]+");
    public static String sanitizeFilename(String filename) {
        // Opera supplies a full path, not just a filename
        filename = lastSegment(filename, '/');
        filename = lastSegment(filename, '\\');
        return FILENAME_SANITIZER.matcher(filename).replaceAll("_");
    }

    private static String lastSegment(String string, char delim) {
        int idx = string.lastIndexOf(delim);
        return idx < 0
                ? string
                : string.substring(idx + 1);
    }

    private String objectKey;

    private String contentType;

    private Long size; // needs to be nullable for historical data

}
