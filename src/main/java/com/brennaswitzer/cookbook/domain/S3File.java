package com.brennaswitzer.cookbook.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.util.regex.Pattern;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class S3File {

    private static final Pattern FILENAME_SANITIZER = Pattern.compile("[^a-zA-Z0-9.\\-]+");
    public static String sanitizeFilename(String filename) {
        return FILENAME_SANITIZER.matcher(filename).replaceAll("_");
    }

    @Getter
    @Setter
    private String objectKey;

    @Getter
    @Setter
    private String contentType;

    @Getter
    @Setter
    private Long size; // needs to be nullable for historical data

}
