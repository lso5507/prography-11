package com.prography.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class QrCode extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private SessionEntity session;

    @Column(nullable = false, unique = true)
    private String hashValue;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean active;

    protected QrCode() {
    }

    public QrCode(SessionEntity session, String hashValue, LocalDateTime expiresAt, boolean active) {
        this.session = session;
        this.hashValue = hashValue;
        this.expiresAt = expiresAt;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public SessionEntity getSession() {
        return session;
    }

    public String getHashValue() {
        return hashValue;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void expireNow() {
        this.active = false;
        this.expiresAt = LocalDateTime.now();
    }
}
