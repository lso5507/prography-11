package com.prography.backend.service;

import com.prography.backend.api.PageResponse;
import com.prography.backend.dto.MemberDto;
import com.prography.backend.entity.Cohort;
import com.prography.backend.entity.CohortMember;
import com.prography.backend.entity.Member;
import com.prography.backend.entity.Part;
import com.prography.backend.entity.Team;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.CohortMemberRepository;
import com.prography.backend.repository.CohortRepository;
import com.prography.backend.repository.MemberRepository;
import com.prography.backend.repository.PartRepository;
import com.prography.backend.repository.TeamRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private static final int INITIAL_DEPOSIT = 100_000;

    private final MemberRepository memberRepository;
    private final CohortRepository cohortRepository;
    private final PartRepository partRepository;
    private final TeamRepository teamRepository;
    private final CohortMemberRepository cohortMemberRepository;
    private final DepositService depositService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public MemberService(MemberRepository memberRepository,
                         CohortRepository cohortRepository,
                         PartRepository partRepository,
                         TeamRepository teamRepository,
                         CohortMemberRepository cohortMemberRepository,
                         DepositService depositService) {
        this.memberRepository = memberRepository;
        this.cohortRepository = cohortRepository;
        this.partRepository = partRepository;
        this.teamRepository = teamRepository;
        this.cohortMemberRepository = cohortMemberRepository;
        this.depositService = depositService;
    }

    @Transactional(readOnly = true)
    public MemberDto.MemberSimpleResponse getMember(Long memberId) {
        Member member = findMember(memberId);
        return new MemberDto.MemberSimpleResponse(member.getId(), member.getLoginId(), member.getName(), member.getRole(),
            member.getStatus());
    }

    @Transactional
    public MemberDto.MemberDetailResponse createMember(MemberDto.CreateMemberRequest request) {
        if (memberRepository.existsByLoginId(request.loginId())) {
            throw new AppException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        Cohort cohort = cohortRepository.findById(request.cohortId())
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_NOT_FOUND));
        Part part = partRepository.findByIdAndCohort(request.partId(), cohort)
            .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));
        Team team = resolveTeam(request.teamId(), cohort);

        Member member = new Member(
            request.loginId(),
            passwordEncoder.encode(request.password()),
            request.name(),
            request.role(),
            com.prography.backend.domain.MemberStatus.ACTIVE
        );
        Member savedMember = memberRepository.save(member);

        CohortMember cohortMember = new CohortMember(cohort, savedMember, part, team, INITIAL_DEPOSIT);
        CohortMember saved = cohortMemberRepository.save(cohortMember);
        depositService.recordInitial(saved, INITIAL_DEPOSIT);
        return toDetail(savedMember, saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberDto.MemberDetailResponse> getAdminMembers(int page, int size) {
        Page<Member> memberPage = memberRepository.findAll(PageRequest.of(page, size));
        List<MemberDto.MemberDetailResponse> content = memberPage.getContent().stream()
            .map(member -> {
                CohortMember cm = cohortMemberRepository.findByMember(member).stream().findFirst().orElse(null);
                return toDetail(member, cm);
            })
            .toList();
        return new PageResponse<>(content, page, size, memberPage.getTotalElements(), memberPage.getTotalPages());
    }

    @Transactional(readOnly = true)
    public MemberDto.MemberDetailResponse getAdminMemberDetail(Long memberId) {
        Member member = findMember(memberId);
        CohortMember cm = cohortMemberRepository.findByMember(member).stream().findFirst().orElse(null);
        return toDetail(member, cm);
    }

    @Transactional
    public MemberDto.MemberDetailResponse updateMember(Long memberId, MemberDto.UpdateMemberRequest request) {
        Member member = findMember(memberId);
        CohortMember cm = cohortMemberRepository.findByMember(member).stream().findFirst()
            .orElseThrow(() -> new AppException(ErrorCode.COHORT_MEMBER_NOT_FOUND));

        Part part = partRepository.findByIdAndCohort(request.partId(), cm.getCohort())
            .orElseThrow(() -> new AppException(ErrorCode.PART_NOT_FOUND));
        Team team = resolveTeam(request.teamId(), cm.getCohort());

        member.update(request.name(), request.role(), request.status());
        cm.updateAssignment(part, team);
        return toDetail(member, cm);
    }

    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = findMember(memberId);
        if (member.getStatus() == com.prography.backend.domain.MemberStatus.WITHDRAWN) {
            throw new AppException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        member.withdraw();
    }

    public Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private Team resolveTeam(Long teamId, Cohort cohort) {
        if (teamId == null) {
            return null;
        }
        return teamRepository.findByIdAndCohort(teamId, cohort)
            .orElseThrow(() -> new AppException(ErrorCode.TEAM_NOT_FOUND));
    }

    private MemberDto.MemberDetailResponse toDetail(Member member, CohortMember cm) {
        if (cm == null) {
            return new MemberDto.MemberDetailResponse(
                member.getId(), member.getLoginId(), member.getName(), member.getRole(), member.getStatus(),
                null, null, null, null, 0, 0
            );
        }
        return new MemberDto.MemberDetailResponse(
            member.getId(),
            member.getLoginId(),
            member.getName(),
            member.getRole(),
            member.getStatus(),
            cm.getId(),
            cm.getCohort().getName(),
            cm.getPart().getName(),
            cm.getTeam() == null ? null : cm.getTeam().getName(),
            cm.getDepositBalance(),
            cm.getExcusedCount()
        );
    }
}
