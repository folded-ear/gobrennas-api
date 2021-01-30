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

    Float focusLeft;
    Float focusTop;
    public boolean hasFocus() {
        return focusLeft != null && focusTop != null;
    }

    public float[] getFocusArray() {
        return hasFocus()
            ? new float[] { focusLeft, focusTop }
            : null;
    }

    public void setFocusArray(float[] focus) {
        if (focus == null) return;
        if (focus.length != 2) {
            throw new IllegalArgumentException("Focus arrays must have two components");
        }
        focusLeft = focus[0];
        focusTop = focus[1];
    }

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
