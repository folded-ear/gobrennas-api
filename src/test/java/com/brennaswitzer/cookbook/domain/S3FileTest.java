package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class S3FileTest {

    @Test
    public void sanitizeFilename() {
        assertEquals("goat.txt",
                     S3File.sanitizeFilename("goat.txt"));
        assertEquals("go_at.txt",
                     S3File.sanitizeFilename("go$$!!at.txt"));
        assertEquals("goat.txt",
                     S3File.sanitizeFilename("c:\\path\\to\\goat.txt"));
        assertEquals("goat.txt",
                     S3File.sanitizeFilename("/path/to/goat.txt"));
        assertEquals("go_at.txt",
                     S3File.sanitizeFilename("/path/to/go!_at.txt"));
    }

}
