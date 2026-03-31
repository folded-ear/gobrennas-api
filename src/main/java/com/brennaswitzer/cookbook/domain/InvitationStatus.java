package com.brennaswitzer.cookbook.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum InvitationStatus implements Identified {
    /**
     * The invitation has been sent (presumably), but not responded to.
     */
    SENT(0L),
    /**
     * The invitation was accepted and friendship established.
     */
    ACCEPTED(100L),
    /**
     * The invitation was withdrawn by the sending user, before the invitee
     * responded to it.
     */
    WITHDRAWN(200L),
    /**
     * The invitation expired before the invitee responded to it. This is lazily
     * evaluated; {@link Invitation#getExpiresAt()} is the source of truth.
     */
    EXPIRED(201L),
    /**
     * The invitation was declined by the invitee.
     */
    DECLINED(300L);

    private final Long id;

}
