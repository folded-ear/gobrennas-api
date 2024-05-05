package com.brennaswitzer.cookbook.domain.envers;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.Instant;

@Entity
@Table(name = "AUD__REVINFO") // two underscores so it sorts first
@RevisionEntity(BfsRevisionListener.class)
@SequenceGenerator(
        name = "aud_seq",
        sequenceName = "aud_seq"
)
@Data
public class BfsRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aud_seq")
    @Column(name = "REV")
    @RevisionNumber
    private long id;

    @Column(name = "REV_TSTMP")
    @RevisionTimestamp
    private long timestamp;

    public Instant getInstant() {
        return Instant.ofEpochMilli(timestamp);
    }

    @Column(name = "REV_USERNAME")
    private String username;

}
