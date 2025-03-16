package project.coca.v1.dto.response.request;

import lombok.Builder;
import lombok.Data;
import project.coca.domain.personal.Member;

@Data
@Builder
public class RequestMemberResponse {
    private String id;
    private String name;
    private String profileImagePath;

    public static RequestMemberResponse of(Member member) {
        return RequestMemberResponse.builder()
                .id(member.getId())
                .name(member.getUserName())
                .profileImagePath(member.getProfileImgPath())
                .build();
    }
}
