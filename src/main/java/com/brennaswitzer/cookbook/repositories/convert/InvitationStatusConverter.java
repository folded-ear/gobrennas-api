package com.brennaswitzer.cookbook.repositories.convert;

import com.brennaswitzer.cookbook.domain.InvitationStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InvitationStatusConverter extends AbstractIdentifiedEnumAttributeConverter<InvitationStatus> {

    public InvitationStatusConverter() {
        super(InvitationStatus.class);
    }

}
