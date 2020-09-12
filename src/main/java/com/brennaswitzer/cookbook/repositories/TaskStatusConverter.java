package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TaskStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class TaskStatusConverter extends AbstractIdentifiedEnumAttributeConverter<TaskStatus> implements AttributeConverter<TaskStatus, Long> {

    public TaskStatusConverter() {
        super(TaskStatus.class);
    }

}
