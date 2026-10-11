package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TextractJobStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TextractJobStatusConverter extends AbstractIdentifiedEnumAttributeConverter<TextractJobStatus> {

    public TextractJobStatusConverter() {
        super(TextractJobStatus.class);
    }

}
