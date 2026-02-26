package com.prography.backend.entity;

import com.prography.backend.domain.AttendanceSource;
import com.prography.backend.domain.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

@Entity
public class Attendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_member_id")
    private CohortMember cohortMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private SessionEntity session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(nullable = false)
    private int lateMinutes;

    @Column(nullable = false)
    private int penaltyAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceSource source;

    @Column(nullable = false)
    private LocalDateTime checkedAt;

    protected Attendance() {
    }

    public Attendance(CohortMember cohortMember, SessionEntity session, AttendanceStatus status, int lateMinutes,
                      int penaltyAmount, AttendanceSource source, LocalDateTime checkedAt) {
        this.cohortMember = cohortMember;
        this.session = session;
        this.status = status;
        this.lateMinutes = lateMinutes;
        this.penaltyAmount = penaltyAmount;
        this.source = source;
        this.checkedAt = checkedAt;
    }

    public Long getId() {
        return id;
    }

    public CohortMember getCohortMember() {
        return cohortMember;
    }

    public SessionEntity getSession() {
        return session;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public int getLateMinutes() {
        return lateMinutes;
    }

    public int getPenaltyAmount() {
        return penaltyAmount;
    }

    public AttendanceSource getSource() {
        return source;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void update(AttendanceStatus status, int lateMinutes, int penaltyAmount) {
        this.status = status;
        this.lateMinutes = lateMinutes;
        this.penaltyAmount = penaltyAmount;
    }
}
