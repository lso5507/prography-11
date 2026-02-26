package com.prography.backend.entity;

import com.prography.backend.domain.MemberRole;
import com.prography.backend.domain.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    protected Member() {
    }

    public Member(String loginId, String password, String name, MemberRole role, MemberStatus status) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.role = role;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public MemberRole getRole() {
        return role;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void update(String name, MemberRole role, MemberStatus status) {
        this.name = name;
        this.role = role;
        this.status = status;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }
}
