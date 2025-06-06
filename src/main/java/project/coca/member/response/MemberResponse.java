package project.coca.member.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.coca.domain.personal.Member;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
public class MemberResponse {
    private String id;
    private String userName;
    private String profileImgPath;
    private List<InterestForTag> interest;

    public static MemberResponse of(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .userName(member.getUserName())
                .profileImgPath(member.getProfileImgPath())
                .interest(member.getInterests().stream().map(InterestForTag::of)
                        .collect(Collectors.toList()))
                .build();
    }
}
