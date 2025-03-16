package project.coca.v1.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.coca.domain.personal.Member;
import project.coca.domain.tag.Interest;
import project.coca.domain.tag.Tag;
import project.coca.security.jwt.JwtRedisService;
import project.coca.security.jwt.JwtTokenProvider;
import project.coca.security.jwt.TokenDto;
import project.coca.v1.dto.request.MemberFunctionRequest;
import project.coca.v1.dto.request.MemberJoinRequest;
import project.coca.v1.dto.request.MemberUpdateRequest;
import project.coca.v1.dto.response.tag.InterestForTag;
import project.coca.v1.repository.InterestRepository;
import project.coca.v1.repository.MemberRepository;
import project.coca.v1.repository.TagRepository;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;
    private final InterestRepository interestRepository;
    private final JwtRedisService jwtRedisService;
    private final String DEFAULT_PROFILE_IMAGE_PATH = "https://cocaattachments.s3.amazonaws.com/DEFAULT_PROFILE_IMG.jpg";
    private final S3Service s3Service;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;

    public MemberService(MemberRepository memberRepository,
                         TagRepository tagRepository,
                         InterestRepository interestRepository, AuthenticationManager AuthenticationManager, JwtTokenProvider jwtTokenProvider, JwtRedisService jwtRedisService, S3Service s3Service) {
        this.memberRepository = memberRepository;
        this.tagRepository = tagRepository;
        this.interestRepository = interestRepository;
        this.authenticationManager = AuthenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtRedisService = jwtRedisService;
        this.s3Service = s3Service;
    }

    //유저프로필URL 가져오기
    public String getMemberProfileUrl(String memberId) {
        Member check = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        return check.getProfileImgPath();
    }

    // ID 중복 확인
    public Boolean checkDuplicationId(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        return memberRepository.findById(id).isEmpty();
    }

    //로그인
    public TokenDto login(MemberFunctionRequest loginMember) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginMember.getId(), loginMember.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = new TokenDto(
                jwtTokenProvider.createAccessToken(authentication),
                jwtTokenProvider.createRefreshToken(authentication.getName())
        );

        return tokenDto;
    }

    public Boolean memberCheck(MemberFunctionRequest loginMember) {
        Member check = memberRepository.findById(loginMember.getId())
                .orElseThrow(() -> new NoSuchElementException("아이디 혹은 비밀번호가 일치하지 않습니다."));

        return loginMember.getPassword().equals(check.getPassword());
    }

    public Boolean logout() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        jwtRedisService.deleteToken(user.getUsername());
        return true;
    }

    //관심사 세팅~
    private List<Interest> setInterest(List<InterestForTag> interestId, Member member) {
        List<Interest> memberInterest = new ArrayList<>();

        if (interestId != null && interestId.size() > 0) {
            for (InterestForTag interest : interestId) {
                Tag tag = tagRepository.findById(interest.getTagId())
                        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 관심사입니다."));

                memberInterest.add(new Interest(member, tag));
            }
        }
        return memberInterest;
    }

    //회원가입
    public Member memberJoin(MemberJoinRequest joinMember, MultipartFile profileImage) throws IOException {
        if (memberRepository.existsById(joinMember.getId()))
            throw new DuplicateKeyException("동일한 아이디의 회원이 이미 존재합니다.");

        Member member = new Member(joinMember.getId(), joinMember.getPassword(),
                joinMember.getUserName());

        //관심사 선택했다면 관심사도 등록 (등록 전에 관심사 있는지 검사)
        List<Interest> memberInterest;
        try {
            memberInterest = setInterest(joinMember.getInterestId(), member);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("존재하지 않는 관심사입니다.");
        }
        //새 관심사 세팅
        member.setInterests(memberInterest);

        // 프로필 이미지 업로드
        if (joinMember.getIsDefaultImage()) {
            member.setProfileImgPath(DEFAULT_PROFILE_IMAGE_PATH);
        } else {
            URL savedUrl = s3Service.uploadProfileImage(profileImage, member.getId());
            member.setProfileImgPath(savedUrl.toString());
        }

        Member join = memberRepository.save(member);
        memberRepository.flush();

        return join;
    }


    //회원탈퇴
    public boolean withdrawal(MemberFunctionRequest withdrawalMember) throws AuthenticationException {
        Member check = memberRepository.findById(withdrawalMember.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        if (withdrawalMember.getPassword().equals(check.getPassword())) {
            memberRepository.delete(check);
            memberRepository.flush();

            //existsById -> 존재하면 true 반환, 존재하지 않으면 false 반환
            //존재하지 않으면 성공이니까 !existsById return.
            return !memberRepository.existsById(withdrawalMember.getId());
        } else
            throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
    }

    //개인정보조회
    public Member memberInfoInquiry(MemberFunctionRequest member) throws AuthenticationException {
        Member inquiryMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        if (member.getPassword().equals(inquiryMember.getPassword()))
            return inquiryMember;
        else
            throw new AuthenticationException("비밀번호가 일치하지 않습니다.");
    }

    //개인정보수정 -> 개인정보조회가 선행이라 pw 확인x
    public Member memberInfoUpdate(MemberUpdateRequest newInfo, MultipartFile profileImage) throws IOException {
        Member member = memberRepository.findById(newInfo.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        if (!newInfo.getPassword().isEmpty() && !newInfo.getPassword().isBlank()) {
            member.setPassword(newInfo.getPassword());
        }
        member.setUserName(newInfo.getUserName());

        //새 관심사 넣기 전 기존 관심사 삭제
        if (member.getInterests() != null && member.getInterests().size() > 0)
            for (Interest interest : member.getInterests())
                interestRepository.delete(interest);

        List<Interest> memberInterest;
        try {
            memberInterest = setInterest(newInfo.getInterestId(), member);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("존재하지 않는 관심사입니다.");
        }
        //새 관심사 세팅
        member.setInterests(memberInterest);
        // 프로필 이미지 업로드
        if (newInfo.getProfileImageUrl().equals(DEFAULT_PROFILE_IMAGE_PATH)) {
            // url이 디폴트 이미지 url과 동일하면 디폴트 이미지
            member.setProfileImgPath(DEFAULT_PROFILE_IMAGE_PATH);
        } else if (!newInfo.getProfileImageUrl().equals(member.getProfileImgPath())) {
            // 본인의 이전 url과 다른 url일 경우
            URL savedUrl = s3Service.uploadProfileImage(profileImage, member.getId());
            member.setProfileImgPath(savedUrl.toString());
        }   // url이 같으면 그냥 pass

        Member check = memberRepository.save(member);
        memberRepository.flush();

        return check;
    }

    public List<InterestForTag> memberTagInquiry(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        List<InterestForTag> memberTag = new ArrayList<>();

        if (member.getInterests() != null && member.getInterests().size() > 0) {
            for (Interest interest : member.getInterests())
                memberTag.add(InterestForTag.of(interest));
        }
        return memberTag;
    }
}
