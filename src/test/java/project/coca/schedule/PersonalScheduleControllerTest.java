package project.coca.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import project.coca.base.ApiResponse;
import project.coca.base.success.ResponseCode;
import project.coca.jwt.CustomUserDetails;
import project.coca.schedule.personal.PersonalSchedule;
import project.coca.schedule.personal.PersonalScheduleController;
import project.coca.schedule.personal.PersonalScheduleService;
import project.coca.schedule.personal.dto.PersonalScheduleRequest;
import project.coca.schedule.personal.dto.PersonalScheduleResponse;
import project.coca.schedule.personal.dto.PersonalScheduleSummaryResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalScheduleControllerTest {

    @Mock
    private PersonalScheduleService personalScheduleService;

    @InjectMocks
    private PersonalScheduleController personalScheduleController;

    @Mock
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        when(customUserDetails.getUsername()).thenReturn("testUser");
    }

    @Test
    void testDeletePersonalScheduleSuccess() {
        doNothing().when(personalScheduleService).deleteById("testUser", 1L);

        ApiResponse<?> response = personalScheduleController.delete(customUserDetails, 1L);

        assertEquals(ResponseCode.OK.getCode(), response.getCode());
        verify(personalScheduleService, times(1)).deleteById("testUser", 1L);
    }

    @Test
    void testAddSuccessful() throws IOException {
        PersonalSchedule schedule = new PersonalSchedule();
        when(personalScheduleService.add(anyString(), any(), any())).thenReturn(schedule);

        ApiResponse<PersonalScheduleResponse> response = personalScheduleController.add(
                customUserDetails, mock(PersonalScheduleRequest.class), new MultipartFile[0]);

        assertEquals(ResponseCode.CREATED.getCode(), response.getCode());
    }

    @Test
    void testGetDetailsSuccessful() {
        List<PersonalSchedule> schedules = Arrays.asList(new PersonalSchedule());
        when(personalScheduleService.findByDates(anyString(), any(), any())).thenReturn(schedules);

        ApiResponse<List<PersonalScheduleResponse>> response = personalScheduleController.getDetails(
                customUserDetails, LocalDate.now());

        assertEquals(ResponseCode.OK.getCode(), response.getCode());
        assertEquals(1, response.getData().size());
    }

    @Test
    void testGetSummaryListSuccessful() {
        List<PersonalSchedule> schedules = Arrays.asList(new PersonalSchedule());
        when(personalScheduleService.findByDates(anyString(), any(), any())).thenReturn(schedules);

        ApiResponse<List<PersonalScheduleSummaryResponse>> response = personalScheduleController.getSummaryList(
                customUserDetails, LocalDate.now(), LocalDate.now().plusDays(7));

        assertEquals(ResponseCode.OK.getCode(), response.getCode());
        assertEquals(1, response.getData().size());
    }

    @Test
    void testUpdateSuccessful() throws IOException {
        PersonalSchedule schedule = new PersonalSchedule();
        when(personalScheduleService.update(anyString(), any(), any())).thenReturn(schedule);

        ApiResponse<PersonalScheduleResponse> response = personalScheduleController.update(
                customUserDetails, mock(PersonalScheduleRequest.class), new MultipartFile[0]);

        assertEquals(ResponseCode.OK.getCode(), response.getCode());
    }
}