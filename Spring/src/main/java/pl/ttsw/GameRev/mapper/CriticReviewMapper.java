package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.CriticReviewDTO;
import pl.ttsw.GameRev.model.CriticReview;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CriticReviewMapper {
    CriticReview toEntity(CriticReviewDTO criticReviewDTO);

    CriticReviewDTO toDto(CriticReview criticReview);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CriticReview partialUpdate(CriticReviewDTO criticReviewDTO, @MappingTarget CriticReview criticReview);
}