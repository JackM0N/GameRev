package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.model.ForumPost;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ForumPostMapper {
    ForumPost toEntity(ForumPostDTO forumPostDTO);

    @AfterMapping
    default void linkForumComments(@MappingTarget ForumPost forumPost) {
        forumPost.getForumComments().forEach(forumComment -> forumComment.setForumPost(forumPost));
    }

    ForumPostDTO toDto(ForumPost forumPost);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ForumPost partialUpdate(ForumPostDTO forumPostDTO, @MappingTarget ForumPost forumPost);
}