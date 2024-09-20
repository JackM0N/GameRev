package pl.ttsw.GameRev;

import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.filter.UserReviewFilter;
import pl.ttsw.GameRev.mapper.ReportMapper;
import pl.ttsw.GameRev.model.Report;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ReportRepository;
import pl.ttsw.GameRev.repository.WebsiteUserRepository;
import pl.ttsw.GameRev.service.ReportService;
import pl.ttsw.GameRev.service.UserReviewService;
import pl.ttsw.GameRev.service.WebsiteUserService;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class ReportServiceIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private WebsiteUserRepository websiteUserRepository;

    @MockBean
    private WebsiteUserService websiteUserService;

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private UserReviewService userReviewService;

    private WebsiteUser testUser;
    private UserReviewDTO testUserReview;
    private Report testReport;
    private final Pageable pageable = PageRequest.ofSize(10);


    @BeforeEach
    public void setup() {
        testUser = websiteUserRepository.findByUsername("testuser").get();
        assertNotNull(testUser);
        UserReviewFilter userReviewFilter = new UserReviewFilter();

        testUserReview = userReviewService.getUserReviewsWithReports(userReviewFilter, pageable).get().findFirst().orElse(null);
        assertNotNull(testUserReview);

        testReport = reportRepository.findById(1L).orElse(null);
        assertNotNull(testReport);
    }

    @Test
    @Transactional
    public void testGetReportById() {
        ReportDTO result = reportService.getReportById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @Transactional
    public void testGetReportsByReview() {
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(testUserReview.getId());

        Page<ReportDTO> result = reportService.getReportsByReview(userReviewDTO, PageRequest.of(0, 10));
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @Transactional
    @WithMockUser("testuser")
    public void testCreateReport_Success() throws BadRequestException {
        when(websiteUserService.getCurrentUser()).thenReturn(testUser);

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setUserReview(testUserReview);
        reportDTO.setContent("New Report Content");

        ReportDTO result = reportService.createReport(reportDTO);

        assertNotNull(result);
        assertNotNull(result.getId());
    }

    @Test
    @Transactional
    @WithMockUser("testuser")
    public void testCreateReport_ReviewDoesNotExist() {
        when(websiteUserService.getCurrentUser()).thenReturn(testUser);

        ReportDTO reportDTO = new ReportDTO();
        UserReviewDTO userReviewDTO = new UserReviewDTO();
        userReviewDTO.setId(999L); // Non-existent review ID
        reportDTO.setUserReview(userReviewDTO);
        reportDTO.setContent("New Report Content");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reportService.createReport(reportDTO);
        });

        assertEquals("User review not found", exception.getMessage());
    }

    @Test
    @Transactional
    @WithMockUser("testuser")
    public void testCreateReport_AlreadyReported() throws BadRequestException {
        when(websiteUserService.getCurrentUser()).thenReturn(testUser);

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setUserReview(testUserReview);
        reportDTO.setContent("New Report Content");

        ReportDTO result = reportService.createReport(reportDTO);
        assertNotNull(result);
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reportService.createReport(reportDTO);
        });
        assertEquals("You've already reported this review", exception.getMessage());
    }

    @Test
    @Transactional
    public void testUpdateReport_Success() throws BadRequestException {
        ReportDTO reportDTO = reportMapper.toDto(testReport);
        reportDTO.setApproved(true);

        ReportDTO result = reportService.updateReport(reportDTO);

        assertNotNull(result);
        assertEquals(true, result.getApproved());
    }

    @Test
    @Transactional
    public void testUpdateReport_ReportDoesNotExist() {
        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setId(999L); // Non-existent report ID

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            reportService.updateReport(reportDTO);
        });

        assertEquals("Report not found", exception.getMessage());
    }
}
