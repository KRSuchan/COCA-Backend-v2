package project.coca.v1.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.coca.domain.personal.Friend;
import project.coca.domain.personal.Member;
import project.coca.domain.personal.PersonalSchedule;
import project.coca.v1.repository.FriendRepository;
import project.coca.v1.repository.MemberRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class FriendService {
    private final FriendRepository friendRepository;
    private final PersonalScheduleService personalScheduleService;
    private final MemberRepository memberRepository;

    public FriendService(FriendRepository friendRepository, PersonalScheduleService personalScheduleService, MemberRepository memberRepository) {
        this.friendRepository = friendRepository;
        this.personalScheduleService = personalScheduleService;
        this.memberRepository = memberRepository;
    }

    /**
     * 31. 친구 등록
     */
    public void saveFriend(Member member1, Member member2) {
        // 3-1. Friend 생성
        Friend friend = new Friend();
        friend.setMember(member1);
        friend.setOpponent(member2);
        friend.setOpponentNickname(member2.getUserName());
        Friend friend2 = new Friend();
        friend2.setMember(member2);
        friend2.setOpponent(member1);
        friend2.setOpponentNickname(member1.getUserName());
        friendRepository.save(friend);
        friendRepository.save(friend2);
    }

    /**
     * 32. 친구 일정 조회 (공개 일정은 표시, 비공개 일정은 "비공개 일정"으로 제목 변환)
     *
     * @return : 일정 start, end, 일정 제목
     */
    public List<PersonalSchedule> findFriendSchedule(Long friendId) {
        Friend friend = friendRepository.findById(friendId)
                .orElseThrow(() -> new NoSuchElementException("친구가 조회되지 않습니다."));
        LocalDate now = LocalDate.now();
        LocalDate sevenDaysAgo = now.minusDays(6);
        LocalDate sevenDaysLater = now.plusDays(6);
        return personalScheduleService.findPersonalSchedulesByDates(friend.getOpponent().getId(), sevenDaysAgo, sevenDaysLater);
    }

    /**
     * 33. 친구 목록 조회
     *
     * @return : 친구 id(Long), 친구 이름,
     */
    public List<Friend> findFriends(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        return friendRepository.findAllByMember(member);
    }

    /**
     * 34. 친구 수정(닉네임 수정)
     */
    public void updateFriend(Friend updateFriend) {
        Friend friend = friendRepository.findById(updateFriend.getId())
                .orElseThrow(() -> new NoSuchElementException("친구가 조회되지 않습니다."));
        memberRepository.findById(updateFriend.getMember().getId())
                .orElseThrow(() -> new NoSuchElementException("친구 관계가 조회되지 않습니다."));
        friend.setOpponentNickname(updateFriend.getOpponentNickname());
        friendRepository.save(friend);
    }

    /**
     * 35. 친구 삭제
     */
    public void deleteFriend(Long friendId) {
        // 친구 관계 조회
        Friend findFriend = friendRepository.findById(friendId)
                .orElseThrow(() -> new NoSuchElementException("친구 관계가 조회되지 않습니다."));
        Member member = memberRepository.findById(findFriend.getMember().getId())
                .orElseThrow(() -> new NoSuchElementException("회원이 조회되지 않습니다."));
        Member opponent = findFriend.getOpponent();
        // 서로 관계가 반대인 친구 관계 조회
        Friend opponentFriend = friendRepository.findByMemberAndOpponent(opponent, member)
                .orElseThrow(() -> new NoSuchElementException("상대방이 조회되지 않습니다."));
        friendRepository.delete(findFriend);
        friendRepository.delete(opponentFriend);
    }
}
