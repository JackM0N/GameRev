package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.ReportDTO;
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

    public ReportService(ReportRepository reportRepository, UserReviewRepository userReviewRepository, WebsiteUserService websiteUserService) {
        this.reportRepository = reportRepository;
        this.userReviewRepository = userReviewRepository;
        this.websiteUserService = websiteUserService;
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


        ReportDTO newReport = ReportMapper.INSTANCE.reportToReportDTO(report);
        System.out.println(newReport);

        return ReportMapper.INSTANCE.reportToReportDTO(reportRepository.save(report));
    }
}
