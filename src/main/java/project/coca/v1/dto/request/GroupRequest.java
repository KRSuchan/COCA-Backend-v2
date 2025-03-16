package project.coca.v1.dto.request;

import lombok.Getter;
import lombok.Setter;
import project.coca.domain.group.CoGroup;
import project.coca.domain.personal.Member;
import project.coca.domain.tag.GroupTag;

import java.util.List;

@Getter
@Setter
public class GroupRequest {
    private Member member;
    private CoGroup group;
    private List<GroupTag> groupTags;
}
