package project.coca.member;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import project.coca.domain.personal.Member;
import project.coca.domain.tag.Interest;
import project.coca.member.request.MemberJoinRequest;
import project.coca.member.response.InterestForTag;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemberServiceIntegrationTest {
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @Commit
    @Order(1)
    public void 회원가입_정상입력() throws Exception {
        //given
        String id = "testID";
        String password = "testPassword";
        String username = "testerName";
        ArrayList<InterestForTag> interestForTags = new ArrayList<>();
        interestForTags.add(new InterestForTag(1L,"스프링"));
        interestForTags.add(new InterestForTag(2L,"자바"));
        interestForTags.add(new InterestForTag(3L,"리액트"));

        Member member = new Member();
        member.setId(id);
        member.setPassword(passwordEncoder.encode(password));
        member.setUserName(username);
        MemberJoinRequest memberJoinRequest = new MemberJoinRequest(
                id,
                password,
                username,
                true,
                interestForTags
        );

        //when
        memberService.memberJoin(memberJoinRequest, null);

        //then
        Member result = memberRepository.findById(member.getId()).get();
        List<Interest> interests = result.getInterests();

        assertEquals(member.getId(), result.getId());
        assertTrue(passwordEncoder.matches(password, result.getPassword()));
        assertEquals(member.getUserName(), result.getUserName());
        int i = 0;
        for (InterestForTag interestForTag : interestForTags) {
            assertEquals(interestForTag.getTagId(), interests.get(i).getTag().getId());
            i++;
        }
    }

    @Test
    @Order(2)
    public void 회원가입_사용가능한ID() throws Exception {
        //given
        String id = "testID2";

        //when
        Boolean usable = memberService.isUsable(id);

        //then
        assertTrue(usable);
        assertEquals(!memberRepository.existsById(id), usable);
    }
    @Test
    @Order(3)
    public void 회원가입_사용불가능한ID() throws Exception {
        //given
        String id = "testID";

        //when
        Boolean usable = memberService.isUsable(id);

        //then
        assertFalse(usable);
        assertEquals(!memberRepository.existsById(id), usable);
    }
}