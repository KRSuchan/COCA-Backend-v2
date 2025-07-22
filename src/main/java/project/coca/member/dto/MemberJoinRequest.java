package project.coca.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.coca.tag.dto.InterestForTag;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class MemberJoinRequest {
    private String id;
    private String password;
    private String userName;
    private Boolean isDefaultImage;
    private List<InterestForTag> interestId;
}
