package pl.ttsw.GameRev.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.mapper.ReportMapper;
import pl.ttsw.GameRev.model.Report;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.ReportRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserReviewRepository userReviewRepository;
    private final WebsiteUserService websiteUserService;
    private final ReportMapper reportMapper;

    public ReportDTO getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));
        return reportMapper.toDto(report);
    }

    public Page<ReportDTO> getReportsByReview(UserReviewDTO userReviewDTO, Pageable pageable) {
        Page<Report> reports = reportRepository.findAllByUserReviewIdAndApprovedIsNullOrApprovedIsTrue(userReviewDTO.getId(), pageable);
        return reports.map(reportMapper::toDto);
    }

    public ReportDTO createReport(ReportDTO reportDTO) throws BadRequestException {
        UserReview userReview = userReviewRepository.findById(reportDTO.getUserReview().getId())
                .orElseThrow(() -> new BadRequestException("User review not found"));
        WebsiteUser currentUser = websiteUserService.getCurrentUser();

        Report report = reportRepository.findByUserAndUserReview(currentUser, userReview)
                .orElse(null);
        if (report != null) {
            throw new BadRequestException("You've already reported this review");
        }

        Report newReport = new Report();
        newReport.setUser(currentUser);
        newReport.setUserReview(userReview);
        newReport.setContent(reportDTO.getContent());
        reportRepository.save(newReport);
        return reportMapper.toDto(newReport);
    }

    public ReportDTO updateReport(ReportDTO reportDTO) throws BadRequestException {
        Report report = reportRepository.findById(reportDTO.getId())
                .orElseThrow(() -> new BadRequestException("Report not found"));
        report.setApproved(reportDTO.getApproved());
        return reportMapper.toDto(reportRepository.save(report));
    }
}
