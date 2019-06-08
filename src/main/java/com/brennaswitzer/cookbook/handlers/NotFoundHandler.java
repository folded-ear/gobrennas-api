package com.brennaswitzer.cookbook.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

@ControllerAdvice
public class NotFoundHandler {
    @Value("${spa.default-file}")
    String defaultFile;

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<String> renderDefaultPage() {
        try {
            InputStream inputStream = getClass().getResourceAsStream(defaultFile);
            String body = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(body);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There was an error completing the action.");
        }
    }

}