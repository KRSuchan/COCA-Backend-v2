package project.coca.v1.dto.request;

import lombok.Getter;
import lombok.Setter;
import project.coca.domain.group.CoGroup;
import project.coca.domain.group.GroupManager;
import project.coca.domain.group.GroupNotice;
import project.coca.domain.personal.Member;
import project.coca.domain.tag.Tag;

import java.util.List;

@Getter
@Setter
public class GroupUpdateRequest {
    private Long groupId;
    private Member admin;
    private CoGroup group;
    private List<Tag> groupTags;
    private List<GroupManager> managers;
    private GroupNotice notice;
    private List<Member> membersToManager;
    private List<Member> managersToMember;
}
