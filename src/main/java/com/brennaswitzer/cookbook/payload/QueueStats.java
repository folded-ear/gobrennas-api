package com.brennaswitzer.cookbook.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueueStats {

    /**
     * Number of items on the queue.
     */
    long size;

    /**
     * Maximum age in seconds of items on the queue, -1 if the queue is empty.
     */
    long maxAge;

    /**
     * Minimum age in seconds of items on the queue, -1 if the queue is empty.
     */
    long minAge;

}
