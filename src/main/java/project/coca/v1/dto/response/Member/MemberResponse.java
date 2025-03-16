package project.coca.v1.dto.response.Member;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.coca.domain.personal.Member;
import project.coca.v1.dto.response.tag.InterestForTag;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
public class MemberResponse {
    private String id;
    private String password;
    private String userName;
    private String profileImgPath;
    private List<InterestForTag> interest = new ArrayList<>();

    public static MemberResponse of(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .password(member.getPassword())
                .userName(member.getUserName())
                .profileImgPath(member.getProfileImgPath())
                .interest(member.getInterests().stream().map(InterestForTag::of)
                        .collect(Collectors.toList()))
                .build();
    }
}
