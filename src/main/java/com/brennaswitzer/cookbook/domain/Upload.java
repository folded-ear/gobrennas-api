package com.brennaswitzer.cookbook.domain;

import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

/**
 * I provide a uniform interface for an "uploaded file", isolating us from the
 * Servlet API and Spring's different opinions on the matter. Factory methods
 * are provided to move from either one's corresponding type to {@code Upload}.
 */
public interface Upload {

    static Upload of(MultipartFile file) {
        if (file == null) return null;
        return new Upload() {

            @Override
            public String getOriginalFilename() {
                return file.getOriginalFilename();
            }

            @Override
            public String getContentType() {
                return file.getContentType();
            }

            @Override
            public long getSize() {
                return file.getSize();
            }

            @Override
            @SneakyThrows(IOException.class)
            public InputStream getInputStream() {
                return file.getInputStream();
            }
        };
    }

    static Upload of(Part part) {
        if (part == null) return null;
        return new Upload() {
            @Override
            public String getOriginalFilename() {
                return part.getSubmittedFileName();
            }

            @Override
            public String getContentType() {
                return part.getContentType();
            }

            @Override
            public long getSize() {
                return part.getSize();
            }

            @Override
            @SneakyThrows(IOException.class)
            public InputStream getInputStream() {
                return part.getInputStream();
            }
        };
    }

    String getOriginalFilename();

    String getContentType();

    long getSize();

    InputStream getInputStream();

}
