package project.coca.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import project.coca.domain.personal.Member;
import project.coca.domain.personal.PersonalSchedule;
import project.coca.member.MemberRepository;
import project.coca.schedule.request.PersonalScheduleRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalScheduleServiceTest {

    @Mock
    private PersonalScheduleRepository personalScheduleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private PersonalScheduleAttachmentRepository personalScheduleAttachmentRepository;

    @InjectMocks
    private PersonalScheduleService personalScheduleService;

    private Member testMember;
    private PersonalSchedule testSchedule;
    private PersonalScheduleRequest testRequest;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .userName("testUser")
                .build();

        testSchedule = PersonalSchedule.builder()
                .id(1L)
                .title("Test Schedule")
                .description("Test Description")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .member(testMember)
                .build();

        testRequest = PersonalScheduleRequest.builder()
                .title("Test Schedule")
                .description("Test Description")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
    }

    @Test
    void add_성공() throws Exception {
        // given
        when(memberRepository.findById("testuser")).thenReturn(Optional.of(testMember));
        when(personalScheduleRepository.save(any(PersonalSchedule.class))).thenReturn(testSchedule);

        // when
        PersonalSchedule result = personalScheduleService.add("testuser", testRequest, null);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Schedule");
        verify(personalScheduleRepository).save(any(PersonalSchedule.class));
    }

    @Test
    void add_회원없음_예외발생() {
        // given
        when(memberRepository.findById("testuser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> personalScheduleService.add("testuser", testRequest, null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("회원이 조회되지 않습니다.");
    }

    @Test
    void findByDates_성공() {
        // given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);
        LocalDateTime startDT = startDate.atStartOfDay();
        LocalDateTime endDT = endDate.atTime(LocalTime.of(23, 59, 59));

        when(personalScheduleRepository.findPersonalScheduleByDateRange("testuser", startDT, endDT))
                .thenReturn(List.of(testSchedule));

        // when
        List<PersonalSchedule> result = personalScheduleService.findByDates("testuser", startDate, endDate);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Schedule");
    }

    @Test
    void update_성공() throws Exception {
        // given
        testRequest.setId(1L);
        when(personalScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        doNothing().when(personalScheduleAttachmentRepository).deleteAllByPersonalSchedule(testSchedule);

        // when
        PersonalSchedule result = personalScheduleService.update("testuser", testRequest, null);

        // then
        assertThat(result).isNotNull();
        verify(personalScheduleRepository).findById(1L);
        verify(personalScheduleAttachmentRepository).deleteAllByPersonalSchedule(testSchedule);
    }

    @Test
    void update_일정없음_예외발생() {
        // given
        testRequest.setId(1L);
        when(personalScheduleRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> personalScheduleService.update("testuser", testRequest, null))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("일정이 조회되지 않습니다.");
    }

    @Test
    void deleteById_성공() {
        // given
        when(memberRepository.findById("testuser")).thenReturn(Optional.of(testMember));
        when(personalScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(personalScheduleAttachmentRepository.findByPersonalSchedule(testSchedule)).thenReturn(List.of());

        // when & then
        personalScheduleService.deleteById("testuser", 1L);
        verify(personalScheduleRepository).deleteById(1L);
    }

    @Test
    void deleteById_회원없음_예외발생() {
        // given
        when(memberRepository.findById("testuser")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> personalScheduleService.deleteById("testuser", 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("회원이 조회되지 않았습니다.");
    }

    @Test
    void deleteById_일정없음_예외발생() {
        // given
        when(memberRepository.findById("testuser")).thenReturn(Optional.of(testMember));
        when(personalScheduleRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> personalScheduleService.deleteById("testuser", 1L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("일정이 조회되지 않았습니다.");
    }
}