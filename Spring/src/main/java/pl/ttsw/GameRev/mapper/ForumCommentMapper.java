package pl.ttsw.GameRev.mapper;

import org.mapstruct.*;
import pl.ttsw.GameRev.dto.ForumCommentDTO;
import pl.ttsw.GameRev.model.ForumComment;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ForumCommentMapper {
    ForumComment toEntity(ForumCommentDTO forumCommentDTO);

    ForumCommentDTO toDto(ForumComment forumComment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ForumComment partialUpdate(ForumCommentDTO forumCommentDTO, @MappingTarget ForumComment forumComment);
}