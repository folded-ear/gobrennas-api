--liquibase formatted sql

--changeset barneyb:add-can-have-multiple-plans-preference
INSERT INTO preference
    (name, type, default_value_str)
VALUES ('canHaveMultiplePlans', 2, 'true');

--changeset barneyb:friendship-model
CREATE TABLE invitation
(
    id                BIGINT      NOT NULL DEFAULT NEXTVAL('id_seq'),
    _eqkey            BIGINT      NOT NULL DEFAULT _eqkey(30),
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_invitation
        PRIMARY KEY (id),

    owner_id BIGINT NOT NULL,
    secret_code       VARCHAR     NOT NULL,
    status            BIGINT      NOT NULL,
    expires_at        TIMESTAMPTZ NOT NULL,
    grant_level       BIGINT,
    grant_entity_type VARCHAR,
    grant_entity_id   BIGINT,
    recipient_id      BIGINT,
    clone_limit       INT         NOT NULL DEFAULT 0,
    clone_count       INT         NOT NULL DEFAULT 0,

    CONSTRAINT fk_invitation_owner_id
        FOREIGN KEY (owner_id)
            REFERENCES users
            ON DELETE CASCADE,
    CONSTRAINT fk_invitation_recipient_id
        FOREIGN KEY (recipient_id)
            REFERENCES users
            ON DELETE CASCADE,
    CONSTRAINT chk_invitation_nneg_clone_limit
        CHECK ( clone_limit >= 0 ),
    CONSTRAINT chk_invitation_clone_count
        CHECK ( clone_count <= clone_limit )
);
CREATE UNIQUE INDEX uk_invitation__eqkey ON invitation (_eqkey);
CREATE INDEX idx_invitation_owner ON invitation (owner_id, created_at);
CREATE INDEX idx_invitation_recipient ON invitation (recipient_id, created_at)
    WHERE recipient_id IS NOT NULL;
CREATE INDEX idx_invitation_secret_code ON invitation (secret_code)
    WHERE status = 0; -- SENT

CREATE TABLE friendship
(
    id            BIGINT      NOT NULL DEFAULT NEXTVAL('id_seq'),
    _eqkey        BIGINT      NOT NULL DEFAULT _eqkey(30),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_friendship
        PRIMARY KEY (id),

    user_id       BIGINT      NOT NULL,
    friend_id     BIGINT      NOT NULL,
    invitation_id BIGINT      NOT NULL,

    CONSTRAINT fk_friendship_user_id
        FOREIGN KEY (user_id)
            REFERENCES users,
    CONSTRAINT fk_friendship_friend_id
        FOREIGN KEY (friend_id)
            REFERENCES users,
    CONSTRAINT fk_friendship_invitation_id
        FOREIGN KEY (invitation_id)
            REFERENCES invitation
);
CREATE UNIQUE INDEX uk_friendship__eqkey ON friendship (_eqkey);
CREATE UNIQUE INDEX uk_friendship_user_id_friend_id ON friendship (user_id, friend_id);
