package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Photo {

    @Embedded
    private S3File file;
    public boolean hasFile() {
        return file != null;
    }

    Float focusTop;
    Float focusLeft;

    public Photo(S3File file) {
        this.file = file;
    }

    public String getObjectKey() {
        return file.getObjectKey();
    }

    public String getContentType() {
        return file.getContentType();
    }

    public Long getSize() {
        return file.getSize();
    }

}
