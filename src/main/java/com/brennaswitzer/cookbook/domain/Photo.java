package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

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

    Float focusLeft;
    Float focusTop;
    public boolean hasFocus() {
        return focusLeft != null && focusTop != null;
    }

    public List<Float> getFocus() {
        return hasFocus()
                ? List.of(focusLeft, focusTop)
                : null;
    }

    public void setFocus(List<Float> focus) {
        if (focus == null) return;
        if (focus.size() != 2) {
            throw new IllegalArgumentException("Focus arrays must have two components");
        }
        focusLeft = focus.get(0);
        focusTop = focus.get(1);
    }

    public Photo(S3File file) {
        this.file = file;
    }

    public String getObjectKey() {
        return file.getObjectKey();
    }

    public String getFilename() {
        return file.getFilename();
    }

    public String getContentType() {
        return file.getContentType();
    }

    public Long getSize() {
        return file.getSize();
    }

    public void clear() {
        file = null;
        focusLeft = null;
        focusTop = null;
    }

}
