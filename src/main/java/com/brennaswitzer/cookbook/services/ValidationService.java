package com.brennaswitzer.cookbook.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@Service
public class ValidationService {

    public ResponseEntity<?> validationService(BindingResult result) {

        if(result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            for(FieldError e : result.getFieldErrors()) {
                errors.put(e.getField(), e.getDefaultMessage());
            }
            return new ResponseEntity<Map<String, String>>(errors, HttpStatus.BAD_REQUEST);
        }

        return null;
    }
}
