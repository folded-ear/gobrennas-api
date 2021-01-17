package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TextractJobInfo {

    private FileInfo photo;
    private boolean ready;
    private Set<TextractJob.Line> lines;

}
