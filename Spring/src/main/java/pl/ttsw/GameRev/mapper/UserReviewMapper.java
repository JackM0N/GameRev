package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.UserReviewDTO;
import pl.ttsw.GameRev.model.UserReview;
import java.time.LocalDate;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING,
        imports = { LocalDate.class })
public interface UserReviewMapper {
    @Mapping(source = "userUsername", target = "user.username")
    @Mapping(source = "gameTitle", target = "game.title")
    @Mapping(target = "postDate", expression = "java(LocalDate.now())")
    @Mapping(target = "positiveRating", constant = "0")
    @Mapping(target = "negativeRating", constant = "0")
    UserReview toEntity(UserReviewDTO userReviewDTO);

    @Mapping(source = "user.username", target = "userUsername")
    @Mapping(source = "game.title", target = "gameTitle")
    UserReviewDTO toDto(UserReview userReview);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "postDate", expression = "java(LocalDate.now())")
    @Mapping(source = "score", target = "score")
    @Mapping(source = "content", target = "content")
    UserReview partialUpdate(UserReviewDTO userReviewDTO, @MappingTarget UserReview userReview);
}
