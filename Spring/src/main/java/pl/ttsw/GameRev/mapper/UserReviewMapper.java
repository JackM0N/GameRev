package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.model.UserReview;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserReviewMapper {
    UserReview toEntity(UserReviewDTO userReviewDTO);

    @Mapping(source = "game.title", target = "gameTitle")
    @Mapping(source = "user.username", target = "userUsername")
    UserReviewDTO toDto(UserReview userReview);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserReview partialUpdate(UserReviewDTO userReviewDTO, @MappingTarget UserReview userReview);
}