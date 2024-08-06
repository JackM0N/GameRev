package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.model.UserReview;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserReviewMapper {
    @Mapping(source = "userUsername", target = "user.username")
    @Mapping(source = "gameTitle", target = "game.title")
    UserReview toEntity(UserReviewDTO userReviewDTO);

    @Mapping(source = "user.username", target = "userUsername")
    @Mapping(source = "game.title", target = "gameTitle")
    UserReviewDTO toDto(UserReview userReview);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    UserReview partialUpdate(UserReviewDTO userReviewDTO, @MappingTarget UserReview userReview);
}