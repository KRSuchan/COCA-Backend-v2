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
public class MemberUpdateRequest {
    private String id;
    private String password;
    private String userName;
    private String profileImageUrl;
    private List<InterestForTag> interestId;
}
