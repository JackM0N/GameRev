package pl.ttsw.GameRev;

import jakarta.persistence.EntityNotFoundException;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.dto.WebsiteUserDTO;
import pl.ttsw.GameRev.mapper.ReportMapper;
import pl.ttsw.GameRev.model.Report;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ReportRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;
import pl.ttsw.GameRev.service.ReportService;
import pl.ttsw.GameRev.service.WebsiteUserService;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserReviewRepository userReviewRepository;

    @Mock
    private WebsiteUserService websiteUserService;

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportService reportService;

    private WebsiteUser testUser;
    private UserReview testUserReview;
    private Report testReport;
    private ReportDTO testReportDTO;
    private UserReviewDTO testUserReviewDTO;
    private WebsiteUserDTO testWebsiteUserDTO;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new WebsiteUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testUserReview = new UserReview();
        testUserReview.setId(1L);
        testUserReview.setUser(testUser);

        testReport = new Report();
        testReport.setId(1L);
        testReport.setUser(testUser);
        testReport.setUserReview(testUserReview);
        testReport.setContent("Inappropriate content");

        testWebsiteUserDTO = new WebsiteUserDTO();
        testWebsiteUserDTO.setId(1L);
        testWebsiteUserDTO.setUsername("testuser");

        testUserReviewDTO = new UserReviewDTO();
        testUserReviewDTO.setId(1L);
        testUserReviewDTO.setUserUsername("testuser");
        testUserReviewDTO.setGameTitle("Test Game");

        testReportDTO = new ReportDTO();
        testReportDTO.setId(1L);
        testReportDTO.setUser(testWebsiteUserDTO);
        testReportDTO.setUserReview(testUserReviewDTO);
        testReportDTO.setContent("Inappropriate content");
    }

    @Test
    public void testGetReportById_Found() {
        when(reportRepository.findById(anyLong())).thenReturn(Optional.of(testReport));
        when(reportMapper.toDto(any(Report.class))).thenReturn(testReportDTO);

        ReportDTO result = reportService.getReportById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetReportById_NotFound() {
        when(reportRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> reportService.getReportById(1L));
    }

    @Test
    public void testGetReportsByReview_Found() {
        Page<Report> reportPage = new PageImpl<>(Collections.singletonList(testReport), PageRequest.of(0, 10), 1);
        when(reportRepository.findAllByUserReviewIdAndApprovedIsNullOrApprovedIsTrue(anyLong(), any(Pageable.class))).thenReturn(reportPage);
        when(reportMapper.toDto(any(Report.class))).thenReturn(testReportDTO);

        Page<ReportDTO> result = reportService.getReportsByReview(testUserReviewDTO, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    public void testGetReportsByReview_NotFound() {
        Page<Report> reportPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(reportRepository.findAllByUserReviewIdAndApprovedIsNullOrApprovedIsTrue(anyLong(), any(Pageable.class))).thenReturn(reportPage);

        Page<ReportDTO> result = reportService.getReportsByReview(testUserReviewDTO, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    public void testCreateReport_Success() throws BadRequestException {
        when(userReviewRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testUserReview));
        when(websiteUserService.getCurrentUser()).thenReturn(testUser);
        when(reportRepository.findByUserAndUserReview(any(WebsiteUser.class), any(UserReview.class))).thenReturn(Optional.empty());
        when(reportMapper.toDto(any(Report.class))).thenReturn(testReportDTO);
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);

        ReportDTO result = reportService.createReport(testReportDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testCreateReport_ReviewDoesNotExist() {
        Optional<UserReview> userReview = Optional.empty();
        when(websiteUserService.getCurrentUser()).thenReturn(testUser);
        when(userReviewRepository.findById(anyLong())).thenReturn(userReview);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reportService.createReport(testReportDTO);
        });

        assertEquals("User review not found", exception.getMessage());
    }

    @Test
    public void testCreateReport_AlreadyReported() {
        when(userReviewRepository.findById(anyLong())).thenReturn(Optional.ofNullable(testUserReview));
        when(websiteUserService.getCurrentUser()).thenReturn(testUser);
        when(reportRepository.findByUserAndUserReview(any(WebsiteUser.class), any(UserReview.class))).thenReturn(Optional.of(testReport));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reportService.createReport(testReportDTO);
        });

        assertEquals("You've already reported this review", exception.getMessage());
    }

    @Test
    public void testUpdateReport_Success() throws BadRequestException {
        when(reportRepository.findById(anyLong())).thenReturn(Optional.of(testReport));
        when(reportMapper.toDto(any(Report.class))).thenReturn(testReportDTO);
        when(reportRepository.save(any(Report.class))).thenReturn(testReport);

        testReportDTO.setApproved(true);
        ReportDTO result = reportService.updateReport(testReportDTO);

        assertNotNull(result);
        assertEquals(true, result.getApproved());
    }

    @Test
    public void testUpdateReport_ReportDoesNotExist() {
        when(reportRepository.findById(anyLong())).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reportService.updateReport(testReportDTO);
        });

        assertEquals("Report not found", exception.getMessage());
    }
}
