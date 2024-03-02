package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.services.LabelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/labels")
public class LabelController {

    @Autowired
    private LabelService labelService;

    @GetMapping("")
    public Iterable<Label> getAllLabels() { return labelService.findAllLabels(); }

    @PostMapping("")
    public ResponseEntity<?> createNewLabel(@Valid @RequestBody Label label, BindingResult result) {
        Label newLabel = labelService.saveOrUpdateLabel(label);
        return new ResponseEntity<>(newLabel, HttpStatus.CREATED);
    }

}
