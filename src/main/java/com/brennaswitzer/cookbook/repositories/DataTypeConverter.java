package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.DataType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DataTypeConverter extends AbstractIdentifiedEnumAttributeConverter<DataType> {

    public DataTypeConverter() {
        super(DataType.class);
    }

}
