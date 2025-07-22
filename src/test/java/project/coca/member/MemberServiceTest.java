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
import project.coca.jwt.JwtProperties;
import project.coca.jwt.JwtRepository;
import project.coca.jwt.JwtTokenProvider;
import project.coca.jwt.UserSession;
import project.coca.jwt.dto.TokenDto;
import project.coca.member.dto.MemberJoinRequest;
import project.coca.schedule.S3Service;
import project.coca.tag.Tag;
import project.coca.tag.TagRepository;
import project.coca.tag.dto.InterestForTag;

import java.util.ArrayList;
import java.util.List;
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
    JwtRepository jwtRepository;
    @Mock
    JwtProperties jwtProperties;
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
        // given
        String id = "tester";
        String password = "1234";
        String username = "tester";
        List<String> roles = List.of("ROLE_USER");
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        long accessExp = 3600000L;
        long refreshExp = 1209600000L;

        // mocking
        Authentication fakeAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(fakeAuthentication);
        when(fakeAuthentication.getName()).thenReturn(username);
        when(jwtTokenProvider.createAccessToken(username)).thenReturn(accessToken);
        when(jwtTokenProvider.createRefreshToken(username)).thenReturn(refreshToken);
        when(jwtProperties.getAccessExpirationTime()).thenReturn(accessExp);
        when(jwtProperties.getRefreshExpirationTime()).thenReturn(refreshExp);
        when(jwtRepository.getSession(accessToken)).thenReturn(new UserSession(username, roles));

        // when
        TokenDto result = memberService.login(id, password);

        // then
        assertEquals(accessToken, result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());
    }
}