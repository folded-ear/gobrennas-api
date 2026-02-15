package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.ValueUtils;
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

    private static final Pattern RE_FILENAME_SANITIZER = Pattern.compile("[^a-zA-Z0-9.-]+");
    private static final Pattern RE_CANON_DASHES = Pattern.compile("_*-[_-]+");
    public static String sanitizeFilename(String filename) {
        if (ValueUtils.noValue(filename)) {
            return "unnamed";
        }
        // Opera supplies a full path, not just a filename
        filename = lastSegment(filename, '/');
        filename = lastSegment(filename, '\\');
        filename = RE_FILENAME_SANITIZER.matcher(filename).replaceAll("_");
        filename = RE_CANON_DASHES.matcher(filename).replaceAll("-");
        return filename;
    }

    public static String sanitizeFilenameWithSuffix(String filename,
                                                    String suffix) {
        filename = sanitizeFilename(filename);
        int idx = filename.lastIndexOf('.');
        if (idx > 0) {
            // move the extension
            suffix += filename.substring(idx);
            filename = filename.substring(0, idx);
        }
        filename += "-" + suffix;
        return filename;
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

    public String getFilename() {
        return lastSegment(objectKey, '/');
    }

}
