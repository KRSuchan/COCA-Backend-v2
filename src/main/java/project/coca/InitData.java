package project.coca;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import project.coca.domain.group.CoGroup;
import project.coca.domain.personal.Member;
import project.coca.domain.tag.Interest;
import project.coca.domain.tag.Tag;
import project.coca.v1.repository.InterestRepository;
import project.coca.v1.repository.MemberRepository;
import project.coca.v1.repository.TagRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InitData {
    private final initService initService;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() throws IOException {
//        log.info("data initialize 시작");
//        initService.init();
//        log.info("data initialize 끝");
    }

    @RequiredArgsConstructor
    @Component
    @Transactional
    public static class initService {
        private final TagRepository tagRepository;
        private final MemberRepository memberRepository;
        private final InterestRepository interestRepository;
        private List<Member> memberList = new ArrayList<>();
        private List<CoGroup> coGroupList = new ArrayList<>();
        private List<Tag> tagList = new ArrayList<>();

        public void init() throws IOException {
            initTag();
            initMember();
        }

        private void initMember() {
            String defaultImgPath = "https://cocaattachments.s3.amazonaws.com/DEFAULT_PROFILE_IMG.jpg";
            // tester0000, tester1111, ..., tester9999 10명 생성
            for (int i = 0; i <= 9; i++) {
                StringBuilder builder = new StringBuilder();
                builder.append(String.valueOf(i).repeat(4));
                initMember("tester" + builder, "tester" + builder, "테스터" + i, defaultImgPath, tagList.subList(0, 3));
            }
            memberList = memberRepository.findAll();
        }

        private void initMember(String id, String pwd, String userName, String profileImgPath, List<Tag> tagList) {
            Member member = new Member(id, pwd, userName, profileImgPath);
            List<Interest> interests = new ArrayList<>();
            for (Tag tag : tagList) {
                interests.add(new Interest(member, tag));
            }
            member.setInterests(interests);
            memberRepository.save(member);
        }

        private void initTag() {
            initTag("IT", "스프링");
            initTag("IT", "자바");
            initTag("IT", "리액트");
            initTag("IT", "자바스크립트");

            initTag("여행", "일본");
            initTag("여행", "미국");
            initTag("여행", "영국");
            initTag("여행", "호주");

            initTag("취업", "공모전");
            initTag("취업", "자격증");
            initTag("취업", "컨설팅");
            initTag("취업", "부트캠프");

            tagList = tagRepository.findAll();
        }

        private void initTag(String field, String name) {
            Tag tag = Tag.builder()
                    .field(field)
                    .name(name)
                    .build();
            tagRepository.save(tag);
        }
    }
}
