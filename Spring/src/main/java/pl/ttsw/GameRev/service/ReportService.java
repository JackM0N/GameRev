package pl.ttsw.GameRev.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.filter.UserReviewFilter;
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

    public Page<ReportDTO> getReportByOwner(
            Pageable pageable
    ) {
        WebsiteUser currentUser = websiteUserService.getCurrentUser();

        Specification<Report> spec = Specification.where((root, query, builder) ->
                builder.equal(root.get("user"), currentUser)
        );

        Page<Report> userReports = reportRepository.findAll(spec, pageable);
        return userReports.map(reportMapper::toDto);
    }

    public ReportDTO createReport(ReportDTO reportDTO) throws BadRequestException {
        UserReview userReview = userReviewRepository.findById(reportDTO.getUserReview().getId())
                .orElseThrow(() -> new BadRequestException("User review not found"));
        WebsiteUser currentUser = websiteUserService.getCurrentUser();

        Report report = reportRepository.findByUserAndUserReview(currentUser, userReview).orElse(null);
        if (report != null) {
            throw new BadRequestException("You've already reported this review");
        }

        Report newReport = reportMapper.toEntity(reportDTO);
        newReport.setUser(currentUser);
        newReport.setUserReview(userReview);
        reportRepository.save(newReport);
        return reportMapper.toDto(newReport);
    }

    public ReportDTO updateReport(ReportDTO reportDTO) throws BadRequestException {
        Report report = reportRepository.findById(reportDTO.getId())
                .orElseThrow(() -> new BadRequestException("Report not found"));
        reportMapper.partialUpdate(reportDTO, report);
        return reportMapper.toDto(reportRepository.save(report));
    }

    public boolean deleteReportById(Long id) throws BadRequestException {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Report not found"));
        reportRepository.delete(report);
        return true;
    }
}
