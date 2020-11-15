package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.time.LocalDate;

public class PlanBucketInfo {

    public static PlanBucketInfo from(PlanBucket bucket) {
        PlanBucketInfo info = new PlanBucketInfo();
        info.setId(bucket.getId());
        info.setName(bucket.getName());
        info.setDate(bucket.getDate());
        return info;
    }

    @NonNull
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String name;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private LocalDate date;

}
