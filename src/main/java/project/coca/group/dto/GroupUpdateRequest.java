package project.coca.group.dto;

import lombok.Getter;
import lombok.Setter;
import project.coca.group.CoGroup;
import project.coca.group.GroupManager;
import project.coca.group.GroupNotice;
import project.coca.member.Member;
import project.coca.tag.Tag;

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
