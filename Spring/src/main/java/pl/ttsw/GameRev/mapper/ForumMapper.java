package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.ForumDTO;
import pl.ttsw.GameRev.model.Forum;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ForumMapper {
    @Mapping(source = "parentForumId", target = "parentForum.id")
    Forum toEntity(ForumDTO forumDTO);

    @Mapping(source = "parentForum.id", target = "parentForumId")
    ForumDTO toDto(Forum forum);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Forum partialUpdate(ForumDTO forumDTO, @MappingTarget Forum forum);
}