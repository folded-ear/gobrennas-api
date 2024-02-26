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
        return FILENAME_SANITIZER.matcher(filename).replaceAll("_");
    }

    private String objectKey;

    private String contentType;

    private Long size; // needs to be nullable for historical data

}
