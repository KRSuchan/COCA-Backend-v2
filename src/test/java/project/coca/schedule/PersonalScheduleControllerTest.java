package project.coca.schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import project.coca.base.ApiResponse;
import project.coca.base.error.ErrorCode;
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
import java.util.NoSuchElementException;

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
    void testAddWithNoSuchElementException() throws IOException {
        when(personalScheduleService.add(anyString(), any(), any()))
                .thenThrow(new NoSuchElementException("Data not found"));

        ApiResponse<?> response = personalScheduleController.add(
                customUserDetails, mock(PersonalScheduleRequest.class), new MultipartFile[0]);

        assertEquals(ErrorCode.NOT_FOUND.getCode(), response.getCode());
    }

    @Test
    void testAddWithUnexpectedException() throws IOException {
        when(personalScheduleService.add(anyString(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ApiResponse<?> response = personalScheduleController.add(
                customUserDetails, mock(PersonalScheduleRequest.class), new MultipartFile[0]);

        assertEquals(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), response.getCode());
    }

    @Test
    void testDeleteNonExistentSchedule() {
        doThrow(new NoSuchElementException("Schedule not found"))
                .when(personalScheduleService).deleteById(anyString(), anyLong());

        ApiResponse<?> response = personalScheduleController.delete(customUserDetails, 999L);

        assertEquals(ErrorCode.NOT_FOUND.getCode(), response.getCode());
    }

    @Test
    void testDeletePersonalScheduleSuccess() {
        doNothing().when(personalScheduleService).deleteById("testUser", 1L);

        ApiResponse<?> response = personalScheduleController.delete(customUserDetails, 1L);

        assertEquals(ResponseCode.OK.getCode(), response.getCode());
        verify(personalScheduleService, times(1)).deleteById("testUser", 1L);
    }

    @Test
    void testGetDetails_NoSchedulesFound() {
        LocalDate testDate = LocalDate.of(2023, 1, 1);
        when(personalScheduleService.findByDates(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new NoSuchElementException("No schedules found"));

        ApiResponse<List<PersonalScheduleResponse>> response = personalScheduleController.getDetails(customUserDetails, testDate);

        assertEquals(ErrorCode.NOT_FOUND.getCode(), response.getCode());
    }

    @Test
    void testGetSummaryList_NoSuchElementException() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(7);

        when(personalScheduleService.findByDates(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new NoSuchElementException("Test exception"));

        ApiResponse<List<PersonalScheduleSummaryResponse>> response = personalScheduleController.getSummaryList(customUserDetails, startDate, endDate);

        assertEquals(ErrorCode.NOT_FOUND.getCode(), response.getCode());
    }

    @Test
    void testUpdateWithNonExistentData() throws IOException {
        when(personalScheduleService.update(anyString(), any(), any()))
                .thenThrow(new NoSuchElementException("Schedule not found"));

        ApiResponse<PersonalScheduleResponse> response = personalScheduleController.update(
                customUserDetails, mock(PersonalScheduleRequest.class), new MultipartFile[0]);

        assertEquals(ErrorCode.NOT_FOUND.getCode(), response.getCode());
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