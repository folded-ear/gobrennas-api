package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.Labeled;
import com.brennaswitzer.cookbook.repositories.LabelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    public Label saveOrUpdateLabel(Label label) {
        return labelRepository.save(label);
    }

    public Iterable<Label> findAllLabels() {
        return labelRepository.findAll();
    }

    public void addLabel(Labeled l, String label) {
        l.addLabel(ensureLabel(label));
    }

    public void removeLabel(Labeled l, String label) {
        l.removeLabel(ensureLabel(label));
    }

    public void updateLabels(Labeled l, Collection<String> labels) {
        l.clearLabels();
        for (String label : labels) {
            addLabel(l, label);
        }
    }

    public Label ensureLabel(String label) {
        String name = label.replaceAll("/+", "-");
        Optional<Label> l = labelRepository.findOneByNameIgnoreCase(name);
        return l.orElseGet(() -> labelRepository.save(new Label(name)));
    }

}
