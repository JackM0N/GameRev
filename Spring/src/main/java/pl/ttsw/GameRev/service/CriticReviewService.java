package pl.ttsw.GameRev.service;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import pl.ttsw.GameRev.dto.CriticReviewDTO;
import pl.ttsw.GameRev.enums.ReviewStatus;
import pl.ttsw.GameRev.mapper.CriticReviewMapper;
import pl.ttsw.GameRev.mapper.GameMapper;
import pl.ttsw.GameRev.model.CriticReview;
import pl.ttsw.GameRev.model.WebsiteUser;
import pl.ttsw.GameRev.repository.CriticReviewRepository;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class CriticReviewService {
    private final CriticReviewRepository criticReviewRepository;
    private final CriticReviewMapper criticReviewMapper;
    private final GameMapper gameMapper;
    private final WebsiteUserService websiteUserService;
    private final GameService gameService;

    public CriticReviewService(CriticReviewRepository criticReviewRepository, CriticReviewMapper criticReviewMapper, GameMapper gameMapper, WebsiteUserService websiteUserService, GameService gameService) {
        this.criticReviewRepository = criticReviewRepository;
        this.criticReviewMapper = criticReviewMapper;
        this.gameMapper = gameMapper;
        this.websiteUserService = websiteUserService;
        this.gameService = gameService;
    }

    public Page<CriticReviewDTO> getAllCriticReviews(
            ReviewStatus reviewStatus,
            LocalDate fromDate,
            LocalDate toDate,
            Integer scoreFrom,
            Integer scoreTo,
            String searchText,
            Pageable pageable
    ) throws BadRequestException {
        Specification<CriticReview> spec = Specification.where(null);

        if (reviewStatus != null) {
            spec = spec.and((root, query, builder) -> builder.equal(root.get("reviewStatus"), reviewStatus));
        }
        if (fromDate != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("postDate"), fromDate));
        }
        if (toDate != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("postDate"), toDate));
        }
        if (scoreFrom != null) {
            spec = spec.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("score"), scoreFrom));
        }
        if (scoreTo != null) {
            spec = spec.and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("score"), scoreTo));
        }
        if (searchText != null) {
            String likePattern = "%" + searchText.toLowerCase() + "%";
            spec = spec.and((root, query, builder) -> builder.or(
                    builder.like(builder.lower(root.get("content")), likePattern),
                    builder.like(builder.lower(root.get("user").get("nickname")), likePattern),
                    builder.like(builder.lower(root.get("user").get("username")), likePattern),
                    builder.like(builder.lower(root.get("user").get("email")), likePattern),
                    builder.like(builder.lower(root.get("game").get("title")), likePattern)
            ));
        }

        Page<CriticReview> criticReviews = criticReviewRepository.findAll(spec, pageable);
        for (CriticReview criticReview : criticReviews) {
            criticReview.getUser().setPassword(null);
            criticReview.getUser().setDescription(null);
        }

        return criticReviews.map(criticReviewMapper::toDto);
    }

    public CriticReviewDTO getCriticReviewById(Long id) {
        Optional<CriticReview> criticReview = criticReviewRepository.findById(id);
        return criticReview.map(criticReviewMapper::toDto).orElse(null);
    }

    public CriticReviewDTO getCriticReviewByTitle(String gameTitle) throws BadRequestException {
        CriticReview criticReview = criticReviewRepository.findByGameTitleAndReviewStatus(gameTitle, ReviewStatus.APPROVED)
                .orElseThrow(() -> new BadRequestException("Critic review not found"));
        criticReview.getUser().setPassword(null);
        criticReview.getUser().setDescription(null);
        return criticReviewMapper.toDto(criticReview);
    }

    public CriticReviewDTO createCriticReview(CriticReviewDTO criticReviewDTO) throws BadRequestException {
        WebsiteUser websiteUser = websiteUserService.getCurrentUser();
        if (websiteUser == null) {
            throw new BadCredentialsException("You have to login first");
        }
        if (criticReviewRepository.findByGameTitle(criticReviewDTO.getGameTitle()).isPresent()){
            throw new BadRequestException("Critic review already exists");
        }
        CriticReview criticReview = new CriticReview();
        criticReview.setPostDate(LocalDate.now());

        criticReview.setGame(gameMapper.toEntity(gameService.getGameByTitle(criticReviewDTO.getGameTitle())));
        criticReview.setUser(websiteUser);
        criticReview.setContent(criticReviewDTO.getContent());
        criticReview.setScore(criticReviewDTO.getScore());
        criticReview.setReviewStatus(ReviewStatus.PENDING);

        return criticReviewMapper.toDto(criticReviewRepository.save(criticReview));
    }

    public CriticReviewDTO updateCriticReview(Long id, CriticReviewDTO criticReviewDTO) throws BadRequestException {
        WebsiteUser websiteUser = websiteUserService.getCurrentUser();
        if (websiteUser == null) {
            throw new BadCredentialsException("You have to login first");
        }
        CriticReview criticReview = criticReviewRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Critic review not found"));

        if (criticReviewDTO.getScore() != null) {
            criticReview.setScore(criticReviewDTO.getScore());
        }
        if (criticReviewDTO.getContent() != null) {
            criticReview.setContent(criticReviewDTO.getContent());
        }
        if (criticReviewDTO.getGameTitle() != null) {
            criticReview.setGame(gameMapper.toEntity(gameService.getGameByTitle(criticReviewDTO.getGameTitle())));
        }

        criticReview.setReviewStatus(ReviewStatus.EDITED);
        criticReview.setStatusChangedBy(websiteUser);

        return criticReviewMapper.toDto(criticReviewRepository.save(criticReview));
    }

    public CriticReviewDTO reviewCriticReview(Long id, ReviewStatus reviewStatus) throws BadRequestException {
        WebsiteUser websiteUser = websiteUserService.getCurrentUser();
        if (websiteUser == null) {
            throw new BadCredentialsException("You have to login first");
        }
        Optional<CriticReview> criticReview = criticReviewRepository.findById(id);
        if (criticReview.isEmpty()) {
            throw new BadRequestException("This review doesnt exist");
        }
        criticReview.get().setReviewStatus(reviewStatus);
        criticReview.get().setStatusChangedBy(websiteUser);
        return criticReviewMapper.toDto(criticReviewRepository.save(criticReview.get()));
    }

    public boolean deleteCriticReview(Long id) throws BadRequestException {
        Optional<CriticReview> criticReview = criticReviewRepository.findById(id);
        if (criticReview.isEmpty()) {
            throw new BadRequestException("This review doesnt exist");
        }
        criticReviewRepository.delete(criticReview.get());
        return true;
    }
}
