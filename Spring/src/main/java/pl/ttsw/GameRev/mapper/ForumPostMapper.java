package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.ForumPostDTO;
import pl.ttsw.GameRev.model.ForumPost;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = SimplifiedUserMapper.class)
public interface ForumPostMapper {
    @Mapping(target = "commentCount", constant = "0")
    @Mapping(target = "views", constant = "0L")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "postDate", expression = "java(LocalDateTime.now())")
    ForumPost toEntity(ForumPostDTO forumPostDTO);

    @AfterMapping
    default void linkForumComments(@MappingTarget ForumPost forumPost) {
        forumPost.getForumComments().forEach(forumComment -> forumComment.setForumPost(forumPost));
    }

    @Mapping(source = "author", target = "author")
    ForumPostDTO toDto(ForumPost forumPost);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "views", ignore = true)
    ForumPost partialUpdate(ForumPostDTO forumPostDTO, @MappingTarget ForumPost forumPost);
}