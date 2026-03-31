package com.brennaswitzer.cookbook.repositories.convert;

import com.brennaswitzer.cookbook.domain.DataType;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DataTypeConverter extends AbstractIdentifiedEnumAttributeConverter<DataType> {

    public DataTypeConverter() {
        super(DataType.class);
    }

}
