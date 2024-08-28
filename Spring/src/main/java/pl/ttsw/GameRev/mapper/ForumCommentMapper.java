package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.model.ForumComment;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = SimplifiedUserMapper.class)
public interface ForumCommentMapper {
    @Mapping(source = "author", target = "author")
    @Mapping(source = "forumPostId", target = "forumPost.id")
    ForumComment toEntity(ForumCommentDTO forumCommentDTO);

    @Mapping(source = "forumPost.id", target = "forumPostId")
    ForumCommentDTO toDto(ForumComment forumComment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", source = "content")
    ForumComment partialUpdateContent(ForumCommentDTO forumCommentDTO, @MappingTarget ForumComment forumComment);
}