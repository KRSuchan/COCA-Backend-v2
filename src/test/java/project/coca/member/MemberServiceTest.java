package project.coca.member;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.coca.auth.jwt.JwtTokenProvider;
import project.coca.auth.jwt.TokenDto;
import project.coca.domain.personal.Member;
import project.coca.domain.tag.Tag;
import project.coca.member.request.MemberJoinRequest;
import project.coca.member.request.MemberLoginRequest;
import project.coca.member.response.InterestForTag;
import project.coca.schedule.S3Service;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @InjectMocks
    MemberService memberService;
    @Mock
    MemberRepository memberRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtTokenProvider jwtTokenProvider;
    @Mock
    TagRepository tagRepository;
    @Mock
    S3Service s3Service;
    @Value("${spring.cloud.aws.s3.url}")
    private String s3url;

    @Test
    public void 회원가입_정상() throws Exception {
        //given
        String id = "testID";
        String password = "testPassword";
        String username = "testerName";
        ArrayList<InterestForTag> interestForTags = new ArrayList<>();
        interestForTags.add(new InterestForTag(1L, "스프링"));
        interestForTags.add(new InterestForTag(2L, "자바"));
        interestForTags.add(new InterestForTag(3L, "리액트"));

        MemberJoinRequest memberJoinRequest = new MemberJoinRequest(
                id,
                password,
                username,
                true,
                interestForTags
        );
        when(memberRepository.existsById(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(new Tag(1L, "IT", "스프링")));
        when(tagRepository.findById(2L)).thenReturn(Optional.of(new Tag(2L, "IT", "자바")));
        when(tagRepository.findById(3L)).thenReturn(Optional.of(new Tag(3L, "IT", "리액트")));
        when(memberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        Member member = memberService.joinMember(memberJoinRequest, null);

        //then
        assertEquals(id, member.getId());
        assertEquals("encoded-password", member.getPassword());
        assertEquals(username, member.getUserName());
        String DEFAULT_PROFILE_IMAGE_PATH = "DEFAULT_PROFILE_IMG.jpg";
        assertEquals(s3url + DEFAULT_PROFILE_IMAGE_PATH, member.getProfileImgPath());
        int i = 0;
        for (InterestForTag interestForTag : interestForTags) {
            assertEquals(interestForTag.getTagId(), member.getInterests().get(i).getTag().getId());
            i++;
        }
    }

    @Test
    public void 회원가입_사용가능한ID() throws Exception {
        //given
        when(memberRepository.findById(any())).thenReturn(Optional.empty());

        //when
        boolean result = memberService.isUsable("test");

        //then
        assertTrue(result);
    }

    @Test
    public void 회원가입_사용불가능한ID() throws Exception {
        //given
        when(memberRepository.findById(any())).thenReturn(Optional.of(new Member()));

        //when
        boolean result = memberService.isUsable("test");

        //then
        assertFalse(result);
    }

    @Test
    public void 로그인_정상() throws Exception {
        //given
        MemberLoginRequest request = MemberLoginRequest.builder()
                .id("testID")
                .password("testPWD")
                .build();
        Authentication fakeAuthentication = mock(Authentication.class);
        when(fakeAuthentication.getName()).thenReturn("testID");

        when(authenticationManager.authenticate(any())).thenReturn(fakeAuthentication);
        when(jwtTokenProvider.createAccessToken(fakeAuthentication.getName())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(fakeAuthentication.getName())).thenReturn("refresh-token");

        //when
        TokenDto tokenDto = memberService.login(request);

        //then
        assertEquals("access-token", tokenDto.getAccessToken());
        assertEquals("refresh-token", tokenDto.getRefreshToken());
    }
}