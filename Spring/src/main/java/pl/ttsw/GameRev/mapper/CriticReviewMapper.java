package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.CriticReviewDTO;
import pl.ttsw.GameRev.enums.ReviewStatus;
import pl.ttsw.GameRev.model.CriticReview;

import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
        uses = SimplifiedUserMapper.class, imports = { LocalDate.class, ReviewStatus.class })
public interface CriticReviewMapper {
    @Mapping(source = "gameTitle", target = "game.title")
    @Mapping(source = "user", target = "user")
    @Mapping(target = "postDate", expression = "java(LocalDate.now())")
    @Mapping(target = "reviewStatus", expression = "java(ReviewStatus.PENDING)")
    CriticReview toEntity(CriticReviewDTO criticReviewDTO);

    @Mapping(source = "game.title", target = "gameTitle")
    CriticReviewDTO toDto(CriticReview criticReview);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CriticReview partialUpdate(CriticReviewDTO criticReviewDTO, @MappingTarget CriticReview criticReview);
}