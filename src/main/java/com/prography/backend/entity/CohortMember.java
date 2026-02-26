package com.prography.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class CohortMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id")
    private Cohort cohort;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "part_id")
    private Part part;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(nullable = false)
    private int depositBalance;

    @Column(nullable = false)
    private int excusedCount;

    protected CohortMember() {
    }

    public CohortMember(Cohort cohort, Member member, Part part, Team team, int depositBalance) {
        this.cohort = cohort;
        this.member = member;
        this.part = part;
        this.team = team;
        this.depositBalance = depositBalance;
        this.excusedCount = 0;
    }

    public Long getId() {
        return id;
    }

    public Cohort getCohort() {
        return cohort;
    }

    public Member getMember() {
        return member;
    }

    public Part getPart() {
        return part;
    }

    public Team getTeam() {
        return team;
    }

    public int getDepositBalance() {
        return depositBalance;
    }

    public int getExcusedCount() {
        return excusedCount;
    }

    public void updateAssignment(Part part, Team team) {
        this.part = part;
        this.team = team;
    }

    public void increaseExcusedCount() {
        this.excusedCount += 1;
    }

    public void decreaseExcusedCount() {
        if (this.excusedCount > 0) {
            this.excusedCount -= 1;
        }
    }

    public void withdrawPenalty(int amount) {
        this.depositBalance -= amount;
    }

    public void refund(int amount) {
        this.depositBalance += amount;
    }
}
