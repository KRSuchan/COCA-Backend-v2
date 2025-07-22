package project.coca.group.dto;

import lombok.Builder;
import lombok.Data;
import project.coca.member.Member;

@Data
@Builder
public class AdminResponse {
    private String id;
    private String userName;
    private String profileImgPath;

    public static AdminResponse of(Member member) {
        return AdminResponse.builder()
                .id(member.getId())
                .userName(member.getUserName())
                .profileImgPath(member.getProfileImgPath())
                .build();
    }
}
