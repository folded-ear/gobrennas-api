package com.brennaswitzer.cookbook.services.textract;

public class JobNotRetryableException extends IllegalStateException {

    public JobNotRetryableException(long jobId) {
        super("Textract job '" + jobId + "' is not retryable");
    }

}
