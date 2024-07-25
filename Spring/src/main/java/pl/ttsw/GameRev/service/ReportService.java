package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ReportDTO;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.mapper.ReportMapper;
import pl.ttsw.GameRev.model.Report;
import pl.ttsw.GameRev.model.UserReview;
import pl.ttsw.GameRev.repository.ReportRepository;
import pl.ttsw.GameRev.repository.UserReviewRepository;
import java.util.Optional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserReviewRepository userReviewRepository;
    private final WebsiteUserService websiteUserService;
    private final ReportMapper reportMapper;

    public ReportService(ReportRepository reportRepository, UserReviewRepository userReviewRepository, WebsiteUserService websiteUserService, ReportMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.userReviewRepository = userReviewRepository;
        this.websiteUserService = websiteUserService;
        this.reportMapper = reportMapper;
    }

    public ReportDTO getReportById(Long id) {
        Optional<Report> reportOptional = reportRepository.findById(id);
        return reportOptional.map(reportMapper::toDto).orElse(null);
    }

    public Page<ReportDTO> getReportsByReview(UserReviewDTO userReviewDTO, Pageable pageable) {
        Page<Report> reports = reportRepository.findAllByUserReviewIdAndApproved(userReviewDTO.getId(),null, pageable);
        if (reports.isEmpty()) {
            return null;
        }
        return reports.map(reportMapper::toDto);
    }

    public ReportDTO createReport(ReportDTO reportDTO) throws BadRequestException {
        UserReview userReview = userReviewRepository.findById(reportDTO.getUserReview().getId());

        if (userReview == null) {
            throw new BadRequestException("This review doesnt exist");
        }

        if(websiteUserService.getCurrentUser() == null){
            throw new BadRequestException("You are not logged in");
        }

        Optional<Report> reportOptional = reportRepository.findByUserAndUserReview(
                websiteUserService.getCurrentUser(),
                userReview
        );


        if (reportOptional.isPresent()) {
            throw new BadRequestException("You've already reported this review");
        }

        Report report = new Report();
        report.setUser(websiteUserService.getCurrentUser());
        report.setUserReview(userReview);
        report.setContent(reportDTO.getContent());

        return reportMapper.toDto(reportRepository.save(report));
    }

    public ReportDTO updateReport(ReportDTO reportDTO) throws BadRequestException {
        Report report = reportRepository.findById(reportDTO.getId()).orElse(null);
        if (report == null) {
            throw new BadRequestException("This report doesnt exist");
        }
        report.setApproved(reportDTO.getApproved());
        return reportMapper.toDto(reportRepository.save(report));
    }
}
