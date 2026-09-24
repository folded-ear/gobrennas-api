package com.brennaswitzer.cookbook.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.Serializable;

/**
 * I permit everything (or throw, if told to), and record what I was last
 * asked, and whether a transaction was active at the time.
 */
class RecordingPermissionEvaluator implements PermissionEvaluator {

    private RuntimeException failure;
    private boolean called;
    private boolean transactionActive;
    private Object targetId;
    private String targetType;
    private Object permission;

    static boolean isTransactionActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }

    @Override
    public boolean hasPermission(Authentication auth,
                                 Object target,
                                 Object permission) {
        return record(target, null, permission);
    }

    @Override
    public boolean hasPermission(Authentication auth,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        return record(targetId, targetType, permission);
    }

    private boolean record(Object targetId,
                           String targetType,
                           Object permission) {
        this.called = true;
        this.transactionActive = isTransactionActive();
        this.targetId = targetId;
        this.targetType = targetType;
        this.permission = permission;
        if (failure != null) throw failure;
        return true;
    }

    void failWith(RuntimeException failure) {
        this.failure = failure;
    }

    boolean wasCalled() {
        return called;
    }

    boolean wasTransactionActive() {
        return transactionActive;
    }

    Object getTargetId() {
        return targetId;
    }

    String getTargetType() {
        return targetType;
    }

    Object getPermission() {
        return permission;
    }

}
