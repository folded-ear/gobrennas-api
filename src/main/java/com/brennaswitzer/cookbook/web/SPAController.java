package com.brennaswitzer.cookbook.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;

@Controller
public class SPAController implements ErrorController {
    @Value("${spa.default-resource}")
    private String defaultResource;

    private final ObjectMapper objectMapper;

    public SPAController(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private class ErrorBody {
        private int status = 500;
        private String message = "Something bad happened.";

        private ErrorBody() {}
        private ErrorBody(int status) {
            this.status = status;
        }
        private ErrorBody(String message) {
            this.message = message;
        }
        private ErrorBody(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    @RequestMapping("/error")
    public ResponseEntity<String> handleError(HttpServletRequest request) throws IOException {
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        ErrorBody body;
        if (status != null) {
            int statusCode = Integer.valueOf(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(
                                StreamUtils.copyToString(
                                        getClass().getResourceAsStream(defaultResource),
                                        Charset.defaultCharset()));
            }
            body = new ErrorBody(statusCode);
        } else {
            body = new ErrorBody();
        }
        if (message != null) {
            body.message = message.toString();
        }
        return ResponseEntity.status(body.status)
                .contentType(MediaType.APPLICATION_JSON)
                    .body(objectMapper.writeValueAsString(
                            body));
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

}
