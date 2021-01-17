package com.brennaswitzer.cookbook.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class S3File {

    @Getter
    @Setter
    private String objectKey;

    @Getter
    @Setter
    private String contentType;

    @Getter
    @Setter
    private Long size; // needs to be nullable for historical data

}
