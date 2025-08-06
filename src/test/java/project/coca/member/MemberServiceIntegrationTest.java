package project.coca.member;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import project.coca.member.dto.MemberJoinRequest;
import project.coca.tag.dto.InterestForTag;

import java.io.IOException;
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
    void 회원가입_정상입력() throws IOException {
        // given
        String id = "testID";
        String password = "testPassword";
        String username = "testerName";
        List<InterestForTag> interestForTags = List.of(
                new InterestForTag(1L, "스프링"),
                new InterestForTag(2L, "자바"),
                new InterestForTag(3L, "리액트")
        );

        MemberJoinRequest request = new MemberJoinRequest(
                id, password, username, true, interestForTags
        );

        // when
        memberService.joinMember(request, null);

        // then
        Member saved = memberRepository.findById(id).orElseThrow();

        assertEquals(id, saved.getId());
        assertTrue(passwordEncoder.matches(password, saved.getPassword()));
        assertEquals(username, saved.getUserName());

        assertEquals(3, saved.getInterests().size());
        for (int i = 0; i < interestForTags.size(); i++) {
            assertEquals(interestForTags.get(i).getTagId(), saved.getInterests().get(i).getTag().getId());
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
    }
}