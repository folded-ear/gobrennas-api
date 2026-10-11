package com.brennaswitzer.cookbook.services.textract;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;

import java.util.Set;

public interface TextractProvider {

    /**
     * I extract the lines of text in the passed photo, or throw a
     * {@link TextractFailure} saying why not.
     */
    Set<TextractJob.Line> detectLines(S3File photo);

}
