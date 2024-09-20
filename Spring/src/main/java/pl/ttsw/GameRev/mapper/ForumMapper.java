package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.model.Forum;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ForumMapper {
    @Mapping(source = "gameTitle", target = "game.title")
    @Mapping(source = "parentForumId", target = "parentForum.id")
    @Mapping(target = "forumModerators", ignore = true)
    @Mapping(target = "postCount", constant = "0")
    @Mapping(target = "isDeleted", constant = "false")
    Forum toEntity(ForumDTO forumDTO);

    @Mapping(source = "parentForum.id", target = "parentForumId")
    @Mapping(source = "game.title", target = "gameTitle")
    @Mapping(source = "forumModerators", target = "forumModerators")
    ForumDTO toDto(Forum forum);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Forum partialUpdate(ForumDTO forumDTO, @MappingTarget Forum forum);
}
