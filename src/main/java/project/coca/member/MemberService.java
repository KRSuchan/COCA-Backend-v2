package project.coca.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import project.coca.aop.ExeTimer;
import project.coca.domain.personal.Member;
import project.coca.domain.tag.Interest;
import project.coca.domain.tag.Tag;
import project.coca.auth.jwt.JwtRedisService;
import project.coca.auth.jwt.JwtTokenProvider;
import project.coca.auth.jwt.TokenDto;
import project.coca.member.request.MemberLoginRequest;
import project.coca.member.request.MemberJoinRequest;
import project.coca.member.request.MemberUpdateRequest;
import project.coca.member.response.InterestForTag;
import project.coca.schedule.S3Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final TagRepository tagRepository;
    private final InterestRepository interestRepository;
    private final JwtRedisService jwtRedisService;
    private final String DEFAULT_PROFILE_IMAGE_PATH = "https://coca-attachments.s3.amazonaws.com/DEFAULT_PROFILE_IMG.jpg";
    private final S3Service s3Service;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    /**
     * 유저프로필URL 가져오기
     * @param memberId
     * @return 회원 프로필 이미지 url
     */
    public String readProfileUrl(String memberId) {
        Member check = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        return check.getProfileImgPath();
    }

    /**
     * ID 중복 확인
     * @param id
     * @return 사용 가능(유니크) : true / 사용 불가(중복) : false
     */
    public Boolean isUsable(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        return memberRepository.findById(id).isEmpty();
    }

    /**
     * 로그인
     * @param loginMember
     * @return TokenDto
     */
    @ExeTimer
    public TokenDto login(MemberLoginRequest loginMember) {
        Authentication authentication  = getMemberAuthentication(loginMember.getId(), loginMember.getPassword());
        // 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = new TokenDto(
                jwtTokenProvider.createAccessToken(authentication),
                jwtTokenProvider.createRefreshToken(authentication.getName())
        );
        return tokenDto;
    }

    /**
     * 개인정보조회 전 비밀번호 확인
     * @param loginMember
     * @return 정상 입력 시 true, 비밀번호 오류 시 false
     * @throws AuthenticationException if authentication fails
     */
    public Boolean checkMember(MemberLoginRequest loginMember){
        try {
            getMemberAuthentication(loginMember.getId(), loginMember.getPassword());
            return true;
        } catch (BadCredentialsException e) {// 비밀번호 틀림
            return false;
        }
    }

    /**
     * 회원 정보 인증
     * @param memberId
     * @param memberPassword
     * @return Authentication
     */
    public Authentication getMemberAuthentication(String memberId, String memberPassword) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(memberId, memberPassword);

        // 2. 실제 검증이 이루어지는 부분 (AuthenticationToken 이용)
        // authenticate 매서드가 실행될 때 UserDetailsServiceImpl.loadUserByUsername() 메서드가 실행
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        return authentication;
    }

    public Boolean logout() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        jwtRedisService.deleteToken(user.getUsername());
        return true;
    }

    /**
     * 관심사 정보 설정
     * @param interestId
     * @param member
     * @return List
     */
    public List<Interest> setInterest(List<InterestForTag> interestId, Member member) {
        List<Interest> memberInterest = new ArrayList<>();

        if (interestId != null && !interestId.isEmpty()) {
            for (InterestForTag interest : interestId) {
                Tag tag = tagRepository.findById(interest.getTagId())
                        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 관심사입니다."));

                memberInterest.add(new Interest(member, tag));
            }
        }
        return memberInterest;
    }

    /**
     * 회원가입
      * @param joinMember
     * @param profileImage
     * @return
     * @throws IOException
     */
    public Member joinMember(MemberJoinRequest joinMember, MultipartFile profileImage) throws IOException {
        if (memberRepository.existsById(joinMember.getId()))
            throw new DuplicateKeyException("동일한 아이디의 회원이 이미 존재합니다.");

        Member member = new Member(joinMember.getId(), passwordEncoder.encode(joinMember.getPassword()),
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
            String savedUrl = s3Service.uploadProfileImage(profileImage, member.getId());
            member.setProfileImgPath(savedUrl);
        }
        Member join = memberRepository.save(member);

        return join;
    }


    /**
     * 회원 탈퇴
     * @param withdrawalMember
     * @return 삭제 성공하면 true 실패하면 false
     * @throws AuthenticationException
     */
    public boolean closeAccount(MemberLoginRequest withdrawalMember) throws AuthenticationException {
        Member check = memberRepository.findById(withdrawalMember.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        getMemberAuthentication(withdrawalMember.getId(), withdrawalMember.getPassword());

        memberRepository.delete(check);
        return !memberRepository.existsById(withdrawalMember.getId());
    }

    /**
     * 회원정보조회
     * @param member
     * @return
     * @throws AuthenticationException
     */
    public Member getMemberInfo(MemberLoginRequest member) throws AuthenticationException {
        Member inquiryMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        return inquiryMember;
    }

    /**
     * 회원정보수정
     * @param newInfo
     * @param profileImage
     * @return Member
     * @throws IOException
     */
    public Member updateMemberInfo(MemberUpdateRequest newInfo, MultipartFile profileImage) throws IOException {
        // 회원 정보 조회
        Member member = memberRepository.findById(newInfo.getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        // password 설정
        if (!newInfo.getPassword().isEmpty() && !newInfo.getPassword().isBlank()) {
            member.setPassword(passwordEncoder.encode(newInfo.getPassword()));
        }
        // 회원 닉네임 설정
        member.setUserName(newInfo.getUserName());

        // 회원 관심사 기존 정보 삭제
        if (member.getInterests() != null && !member.getInterests().isEmpty())
            for (Interest interest : member.getInterests())
                interestRepository.delete(interest);
        // 새 관심사 설정
        List<Interest> memberInterest;
        try {
            memberInterest = setInterest(newInfo.getInterestId(), member);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("존재하지 않는 관심사입니다.");
        }
        member.setInterests(memberInterest);

        // 프로필 이미지 설정
        if (newInfo.getProfileImageUrl().equals(DEFAULT_PROFILE_IMAGE_PATH)) {
            // url이 디폴트 이미지 url과 동일하면 디폴트 이미지
            member.setProfileImgPath(DEFAULT_PROFILE_IMAGE_PATH);
        } else if (!newInfo.getProfileImageUrl().equals(member.getProfileImgPath())) {
            // 본인의 이전 url과 다른 url일 경우
            String savedUrl = s3Service.uploadProfileImage(profileImage, member.getId());
            member.setProfileImgPath(savedUrl);
        }   // url이 같으면 그냥 pass

        // 최종 save
        Member check = memberRepository.save(member);

        return check;
    }

    /**
     * 회원 관심태그 조회
     * @param memberId
     * @return List<InterestForTag>
     */
    public List<InterestForTag> getMemberTags(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));

        List<InterestForTag> memberTag = new ArrayList<>();

        if (member.getInterests() != null && !member.getInterests().isEmpty()) {
            for (Interest interest : member.getInterests())
                memberTag.add(InterestForTag.of(interest));
        }
        return memberTag;
    }
}
